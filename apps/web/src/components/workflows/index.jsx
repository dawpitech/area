import React, { useEffect, useMemo, useRef, useState } from 'react'
import {
  apiGetWorkflows,
  apiGetWorkflow,
  apiCreateWorkflow,
  apiCheckWorkflow,
  apiPatchWorkflow,
  apiDeleteWorkflow,
  apiGetActions,
  apiGetActionDetails,
  apiGetModifiers,
  apiGetModifierDetails,
  apiGetReactions,
  apiGetReactionDetails,
  apiGetWorkflowLogs,
} from '../../api/auth'
import { useAuth } from '../../contexts/authContext'

const safeObj = (v) => (v && typeof v === 'object' && !Array.isArray(v) ? v : {})
const buildDefaults = (details) => {
  const out = {}
  const ps = Array.isArray(details?.Parameters) ? details.Parameters : []
  for (const p of ps) {
    if (!p?.Name) continue
    const t = (p.Type || '').toLowerCase()
    if (t === 'string') out[p.Name] = ''
    else if (['int', 'integer', 'number', 'float', 'double'].includes(t)) out[p.Name] = 0
    else if (['bool', 'boolean'].includes(t)) out[p.Name] = false
    else out[p.Name] = ''
  }
  return out
}
const coerce = (type, raw) => {
  const t = (type || '').toLowerCase()
  if (['bool', 'boolean'].includes(t)) return Boolean(raw)
  if (['int', 'integer'].includes(t)) {
    const n = Number(raw)
    return Number.isFinite(n) ? Math.trunc(n) : 0
  }
  if (['number', 'float', 'double'].includes(t)) {
    const n = Number(raw)
    return Number.isFinite(n) ? n : 0
  }
  return raw ?? ''
}

const DetailsPanel = ({ label, loading, details }) => (
  <div className="mt-3 p-3 rounded-lg border border-gray-200 dark:border-gray-800 bg-white/60 dark:bg-black/20">
    {loading && !details ? (
      <p className="text-sm text-gray-500 dark:text-gray-400">Loading {label.toLowerCase()} details...</p>
    ) : details ? (
      <>
        <p className="text-sm text-gray-800 dark:text-gray-100 font-medium">{details.PrettyName || details.Name}</p>
        {details.Description ? (
          <p className="text-sm text-gray-600 dark:text-gray-300 mt-1">{details.Description}</p>
        ) : null}
        {Array.isArray(details.Parameters) && details.Parameters.length > 0 ? (
          <div className="mt-2">
            <p className="text-xs uppercase tracking-wide text-gray-500 dark:text-gray-400">Parameters</p>
            <ul className="mt-1 list-disc list-inside text-sm text-gray-700 dark:text-gray-200">
              {details.Parameters.map((p) => (
                <li key={p.Name}>
                  <span className="font-medium">{p.PrettyName || p.Name}</span>
                  <span className="text-gray-500 dark:text-gray-400"> ({p.Type})</span>
                </li>
              ))}
            </ul>
          </div>
        ) : (
          <p className="text-sm text-gray-500 dark:text-gray-400 mt-2">No parameters.</p>
        )}
      </>
    ) : (
      <p className="text-sm text-gray-500 dark:text-gray-400">No details.</p>
    )}
  </div>
)

const SelectWithDetails = ({
  label,
  required,
  optionalLabel,
  loading,
  options,
  value,
  setValue,
  detailsByName,
  detailsLoading,
  selectedDetails,
  emptyOptionsText,
}) => {
  const placeholder = required ? `Select a ${label.toLowerCase()}` : (optionalLabel || `No ${label.toLowerCase()}`)
  return (
    <div>
      <label className="block text-sm font-medium text-gray-700 dark:text-gray-200">{label}</label>
      <select
        value={value}
        onChange={(e) => setValue(e.target.value)}
        className="mt-1 w-full px-3 py-2 border rounded-lg outline-none focus:border-indigo-600 transition
                   bg-white text-gray-900 border-gray-300
                   dark:bg-gray-900 dark:text-gray-100 dark:border-gray-700"
        required={required}
        disabled={loading}
      >
        {required ? (
          <option value="" disabled>{loading ? `Loading ${label.toLowerCase()}...` : placeholder}</option>
        ) : (
          <option value="">{loading ? `Loading ${label.toLowerCase()}...` : placeholder}</option>
        )}
        {options.length === 0 && !loading && emptyOptionsText ? (
          <option value="" disabled>{emptyOptionsText}</option>
        ) : null}
        {options.map((n) => (
          <option key={n} value={n}>{detailsByName?.[n]?.PrettyName || n}</option>
        ))}
      </select>
      {value ? <DetailsPanel label={label} loading={detailsLoading} details={selectedDetails} /> : null}
    </div>
  )
}

const ParamsEditor = ({ title, details, values, setKV }) => {
  if (!details) return null
  const ps = Array.isArray(details.Parameters) ? details.Parameters : []
  if (ps.length === 0) {
    return (
      <div className="p-3 rounded-lg border border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-900">
        <p className="text-sm text-gray-500 dark:text-gray-400">No {title.toLowerCase()} parameters.</p>
      </div>
    )
  }
  return (
    <div className="p-3 rounded-lg border border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-900 space-y-3">
      <p className="text-sm font-semibold text-gray-700 dark:text-gray-200">{title} parameters</p>
      {ps.map((p) => {
        const key = p.Name
        const type = (p.Type || '').toLowerCase()
        const label = p.PrettyName || p.Name
        const val = values?.[key]
        if (['bool', 'boolean'].includes(type)) {
          return (
            <div key={key} className="flex items-center gap-2">
              <input
                id={`${title}-${key}`}
                type="checkbox"
                checked={Boolean(val)}
                onChange={(e) => setKV(key, 'boolean', e.target.checked)}
              />
              <label htmlFor={`${title}-${key}`} className="text-sm text-gray-700 dark:text-gray-200">{label}</label>
            </div>
          )
        }
        if (['int', 'integer', 'number', 'float', 'double'].includes(type)) {
          return (
            <div key={key}>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-200">{label}</label>
              <input
                type="number"
                value={val ?? 0}
                onChange={(e) => setKV(key, type, e.target.value)}
                className="mt-1 w-full px-3 py-2 border rounded-lg outline-none focus:border-indigo-600 transition
                           bg-white text-gray-900 border-gray-300
                           dark:bg-gray-950 dark:text-gray-100 dark:border-gray-700"
              />
            </div>
          )
        }
        return (
          <div key={key}>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-200">{label}</label>
            <input
              type="text"
              value={val ?? ''}
              onChange={(e) => setKV(key, type, e.target.value)}
              className="mt-1 w-full px-3 py-2 border rounded-lg outline-none focus:border-indigo-600 transition
                         bg-white text-gray-900 border-gray-300
                         dark:bg-gray-950 dark:text-gray-100 dark:border-gray-700"
            />
          </div>
        )
      })}
    </div>
  )
}

const Toast = ({ type = 'success', message, onClose }) => {
  if (!message) return null
  const base =
    'fixed bottom-6 right-6 z-[60] max-w-md w-[92vw] sm:w-auto px-4 py-3 rounded-lg shadow-lg border text-sm'
  const styles =
    type === 'error'
      ? 'bg-red-50 border-red-200 text-red-800 dark:bg-red-950/40 dark:border-red-900 dark:text-red-200'
      : 'bg-green-50 border-green-200 text-green-800 dark:bg-green-950/40 dark:border-green-900 dark:text-green-200'
  return (
    <div className={`${base} ${styles}`} role="status" aria-live="polite">
      <div className="flex items-start gap-3">
        <p className="flex-1 leading-snug">{message}</p>
        <button
          type="button"
          onClick={onClose}
          className="text-xs font-semibold px-2 py-1 rounded-md border border-current/20 hover:bg-black/5 dark:hover:bg-white/10 transition"
        >
          Close
        </button>
      </div>
    </div>
  )
}

const Workflows = () => {
  const { token } = useAuth()

  const [workflows, setWorkflows] = useState([])
  const [selectedId, setSelectedId] = useState(null)
  const [detailsById, setDetailsById] = useState({})
  const [loadingList, setLoadingList] = useState(true)
  const [loadingDetails, setLoadingDetails] = useState(false)
  const [error, setError] = useState('')

  const [mode, setMode] = useState('view')
  const isCreate = mode === 'create'
  const isEdit = mode === 'view' && selectedId != null
  const [saving, setSaving] = useState(false)

  const [name, setName] = useState('')
  const [active, setActive] = useState(true)

  const [actions, setActions] = useState([])
  const [loadingActions, setLoadingActions] = useState(false)
  const [actionDetailsByName, setActionDetailsByName] = useState({})
  const [loadingActionDetails, setLoadingActionDetails] = useState(false)
  const [selectedActionDetails, setSelectedActionDetails] = useState(null)

  const [modifiers, setModifiers] = useState([])
  const [loadingModifiers, setLoadingModifiers] = useState(false)
  const [modifierDetailsByName, setModifierDetailsByName] = useState({})
  const [loadingModifierDetails, setLoadingModifierDetails] = useState(false)
  const [selectedModifierDetails, setSelectedModifierDetails] = useState(null)

  const [reactions, setReactions] = useState([])
  const [loadingReactions, setLoadingReactions] = useState(false)
  const [reactionDetailsByName, setReactionDetailsByName] = useState({})
  const [loadingReactionDetails, setLoadingReactionDetails] = useState(false)
  const [selectedReactionDetails, setSelectedReactionDetails] = useState(null)

  const [actionName, setActionName] = useState('')
  const [modifierName, setModifierName] = useState('')
  const [reactionName, setReactionName] = useState('')

  const [actionParams, setActionParams] = useState({})
  const [modifierParams, setModifierParams] = useState({})
  const [reactionParams, setReactionParams] = useState({})

  const preloadedActionsRef = useRef(new Set())
  const preloadedModifiersRef = useRef(new Set())
  const preloadedReactionsRef = useRef(new Set())

  const [logsOpen, setLogsOpen] = useState(false)
  const [logsLoading, setLogsLoading] = useState(false)
  const [logsError, setLogsError] = useState('')
  const [logs, setLogs] = useState([])
  const [logsQuery, setLogsQuery] = useState('')

  const [toast, setToast] = useState({ type: 'success', message: '' })
  const toastTimerRef = useRef(null)

  const showToast = (type, message) => {
    if (toastTimerRef.current) window.clearTimeout(toastTimerRef.current)
    setToast({ type, message })
    toastTimerRef.current = window.setTimeout(() => {
      setToast({ type: 'success', message: '' })
      toastTimerRef.current = null
    }, 3500)
  }

  useEffect(() => {
    return () => {
      if (toastTimerRef.current) window.clearTimeout(toastTimerRef.current)
    }
  }, [])

  const selectedWorkflowLight = useMemo(
    () => workflows.find(w => (w.WorkflowID ?? w.ID ?? w.id) === selectedId) || null,
    [workflows, selectedId]
  )
  const selectedWorkflowDetails = useMemo(
    () => (selectedId != null ? detailsById[selectedId] : null),
    [detailsById, selectedId]
  )

  const resetForm = () => {
    setName('')
    setActive(true)
    setActionName('')
    setModifierName('')
    setReactionName('')
    setActionParams({})
    setModifierParams({})
    setReactionParams({})
    setSelectedActionDetails(null)
    setSelectedModifierDetails(null)
    setSelectedReactionDetails(null)
  }

  const hydrate = (wf) => {
    if (!wf) return
    setName(wf.Name ?? '')
    setActive(Boolean(wf.Active))
    setActionName(wf.ActionName ?? '')
    setModifierName(wf.ModifierName ?? '')
    setReactionName(wf.ReactionName ?? '')
    setActionParams(safeObj(wf.ActionParameters))
    setModifierParams(safeObj(wf.ModifierParameters))
    setReactionParams(safeObj(wf.ReactionParameters))
  }

  const kvAction = (k, t, raw) => setActionParams(p => ({ ...p, [k]: coerce(t, raw) }))
  const kvModifier = (k, t, raw) => setModifierParams(p => ({ ...p, [k]: coerce(t, raw) }))
  const kvReaction = (k, t, raw) => setReactionParams(p => ({ ...p, [k]: coerce(t, raw) }))

  const preloadList = async (names, refSet, fetchOne, setCache) => {
    await Promise.allSettled(
      names.map(async (n) => {
        if (refSet.current.has(n)) return
        refSet.current.add(n)
        const d = await fetchOne(n)
        setCache(prev => (prev[n] ? prev : { ...prev, [n]: d }))
      })
    )
  }

  useEffect(() => {
    if (!token) {
      setLoadingList(false)
      return
    }
    ;(async () => {
      try {
        setLoadingList(true)
        setError('')
        const data = await apiGetWorkflows()
        const list = Array.isArray(data) ? data : []
        setWorkflows(list)
        const first = list[0]
        setSelectedId(first ? (first.WorkflowID ?? first.ID ?? first.id) : null)
      } catch (e) {
        console.error(e)
        setError(e.message || 'Failed to load workflows.')
      } finally {
        setLoadingList(false)
      }
    })()
  }, [token])

  useEffect(() => {
    if (!token) return
    let cancel = false
    ;(async () => {
      try {
        setLoadingActions(true)
        const data = await apiGetActions()
        const list = Array.isArray(data?.ActionsName) ? data.ActionsName : []
        if (cancel) return
        setActions(list)
        await preloadList(list, preloadedActionsRef, apiGetActionDetails, setActionDetailsByName)
      } catch (e) {
        console.error(e)
      } finally {
        if (!cancel) setLoadingActions(false)
      }
    })()
    return () => { cancel = true }
  }, [token])

  useEffect(() => {
    if (!token) return
    let cancel = false
    ;(async () => {
      try {
        setLoadingModifiers(true)
        const data = await apiGetModifiers()
        const list = Array.isArray(data?.ModifiersName) ? data.ModifiersName : []
        if (cancel) return
        setModifiers(list)
        await preloadList(list, preloadedModifiersRef, apiGetModifierDetails, setModifierDetailsByName)
      } catch (e) {
        console.error(e)
      } finally {
        if (!cancel) setLoadingModifiers(false)
      }
    })()
    return () => { cancel = true }
  }, [token])

  useEffect(() => {
    if (!token) return
    let cancel = false
    ;(async () => {
      try {
        setLoadingReactions(true)
        const data = await apiGetReactions()
        const list = Array.isArray(data?.ReactionsName) ? data.ReactionsName : []
        if (cancel) return
        setReactions(list)
        await preloadList(list, preloadedReactionsRef, apiGetReactionDetails, setReactionDetailsByName)
      } catch (e) {
        console.error(e)
      } finally {
        if (!cancel) setLoadingReactions(false)
      }
    })()
    return () => { cancel = true }
  }, [token])

  useEffect(() => {
    if (!token || selectedId == null) return
    const cached = detailsById[selectedId]
    if (cached) {
      hydrate(cached)
      setMode('view')
      return
    }
    let cancel = false
    ;(async () => {
      try {
        setLoadingDetails(true)
        setError('')
        const full = await apiGetWorkflow(selectedId)
        if (cancel) return
        setDetailsById(prev => ({ ...prev, [selectedId]: full }))
        hydrate(full)
        setMode('view')
      } catch (e) {
        console.error(e)
        if (!cancel) setError(e.message || 'Failed to load workflow details.')
      } finally {
        if (!cancel) setLoadingDetails(false)
      }
    })()
    return () => { cancel = true }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token, selectedId])

  const ensureDetails = async (name, cache, setSelected, setLoading, fetchOne, setCache, setParams) => {
    if (!name) { setSelected(null); return }
    const c = cache[name]
    if (c) {
      setSelected(c)
      setParams(p => (Object.keys(p || {}).length ? p : buildDefaults(c)))
      return
    }
    try {
      setLoading(true)
      const d = await fetchOne(name)
      setCache(prev => ({ ...prev, [name]: d }))
      setSelected(d)
      setParams(p => (Object.keys(p || {}).length ? p : buildDefaults(d)))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (!token) return
    ensureDetails(actionName, actionDetailsByName, setSelectedActionDetails, setLoadingActionDetails, apiGetActionDetails, setActionDetailsByName, setActionParams)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token, actionName])

  useEffect(() => {
    if (!token) return
    if (!modifierName) { setSelectedModifierDetails(null); return }
    ensureDetails(modifierName, modifierDetailsByName, setSelectedModifierDetails, setLoadingModifierDetails, apiGetModifierDetails, setModifierDetailsByName, setModifierParams)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token, modifierName])

  useEffect(() => {
    if (!token) return
    ensureDetails(reactionName, reactionDetailsByName, setSelectedReactionDetails, setLoadingReactionDetails, apiGetReactionDetails, setReactionDetailsByName, setReactionParams)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token, reactionName])

  const onCreate = () => { setMode('create'); resetForm() }
  const onCancel = () => {
    setMode('view')
    if (selectedWorkflowDetails) hydrate(selectedWorkflowDetails)
    else resetForm()
  }

  const payload = (id) => ({
    WorkflowID: id ?? 0,
    Name: name.trim(),
    Active: active,
    ActionName: actionName.trim(),
    ActionParameters: actionParams ?? {},
    ModifierName: modifierName.trim(),
    ModifierParameters: modifierParams ?? {},
    ReactionName: reactionName.trim(),
    ReactionParameters: reactionParams ?? {},
  })

  const onSave = async (e) => {
    e.preventDefault()
    if (!token) return showToast('error', 'You must be logged in.')
    if (!name.trim()) return showToast('error', 'Name is required.')
    if (!actionName.trim() || !reactionName.trim()) return showToast('error', 'Action and Reaction are required.')
    if (saving) return

    try {
      setSaving(true)
      setError('')
      let id = selectedId
      const wasCreate = isCreate

      if (isCreate) {
        const created = await apiCreateWorkflow({ WorkflowID: 0, Name: name.trim(), Active: active })
        id = created?.WorkflowID ?? created?.ID ?? created?.id
        if (id == null) throw new Error('Missing WorkflowID in create response.')
      }

      const full = payload(id)
      const checkRes = await apiCheckWorkflow(full)
      if (!Boolean(checkRes?.SyntaxValid)) {
        const msg = checkRes?.Error || checkRes?.error || 'The workflow is invalid.'
        showToast('error', msg)
        return
      }

      const patched = await apiPatchWorkflow(id, full)

      setWorkflows(prev => {
        const exists = prev.some(w => (w.WorkflowID ?? w.ID ?? w.id) === id)
        const light = { WorkflowID: patched?.WorkflowID ?? id, Name: patched?.Name ?? full.Name, Active: patched?.Active ?? full.Active }
        return exists
          ? prev.map(w => ((w.WorkflowID ?? w.ID ?? w.id) === id ? { ...w, ...light } : w))
          : [...prev, light]
      })

      setDetailsById(prev => ({ ...prev, [id]: patched }))
      setSelectedId(id)
      setMode('view')
      hydrate(patched)

      showToast('success', wasCreate ? 'Workflow created successfully.' : 'Workflow updated successfully.')
    } catch (err) {
      console.error(err)
      const msg = err?.message || 'Failed to save workflow.'
      setError(msg)
      showToast('error', msg)
    } finally {
      setSaving(false)
    }
  }

  const onDelete = async () => {
    if (!selectedId) return
    if (!window.confirm('Delete this workflow? This action cannot be undone.')) return
    try {
      setSaving(true)
      setError('')
      await apiDeleteWorkflow(selectedId)
      setWorkflows(prev => prev.filter(w => (w.WorkflowID ?? w.ID ?? w.id) !== selectedId))
      setDetailsById(prev => {
        const c = { ...prev }
        delete c[selectedId]
        return c
      })
      setSelectedId(null)
      resetForm()
      setMode('view')
      showToast('success', 'Workflow deleted successfully.')
    } catch (err) {
      console.error(err)
      const msg = err?.message || 'Failed to delete workflow.'
      setError(msg)
      showToast('error', msg)
    } finally {
      setSaving(false)
    }
  }

  const normalizeLogsResponse = (data) => {
    if (Array.isArray(data)) return data
    if (Array.isArray(data?.Logs)) return data.Logs
    if (Array.isArray(data?.logs)) return data.logs
    return []
  }

  const loadLogs = async (wfId) => {
    if (!token) return
    if (wfId == null) return
    try {
      setLogsError('')
      setLogsLoading(true)
      const data = await apiGetWorkflowLogs(wfId)
      const list = normalizeLogsResponse(data)
      const sorted = [...list].sort((a, b) => {
        const ta = Date.parse(a?.Timestamp ?? a?.timestamp ?? '') || 0
        const tb = Date.parse(b?.Timestamp ?? b?.timestamp ?? '') || 0
        return tb - ta
      })
      setLogs(sorted)
    } catch (e) {
      console.error(e)
      setLogsError(e?.message || 'Failed to load logs.')
      setLogs([])
    } finally {
      setLogsLoading(false)
    }
  }

  const openLogs = async () => {
    if (!selectedId) return
    setLogsOpen(true)
    setLogsQuery('')
    await loadLogs(selectedId)
  }

  const closeLogs = () => {
    setLogsOpen(false)
    setLogsError('')
    setLogsQuery('')
  }

  const filteredLogs = useMemo(() => {
    const q = (logsQuery || '').trim().toLowerCase()
    if (!q) return logs
    return logs.filter((l) => {
      const msg = String(l?.Message ?? l?.message ?? '').toLowerCase()
      const typ = String(l?.Type ?? l?.type ?? '').toLowerCase()
      const ts = String(l?.Timestamp ?? l?.timestamp ?? '').toLowerCase()
      return msg.includes(q) || typ.includes(q) || ts.includes(q)
    })
  }, [logs, logsQuery])

  useEffect(() => {
    if (!logsOpen) return
    const onKey = (e) => {
      if (e.key === 'Escape') closeLogs()
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [logsOpen])

  const title = isCreate
    ? 'Create a new workflow'
    : selectedWorkflowDetails?.Name || selectedWorkflowLight?.Name || (selectedId != null ? `Workflow #${selectedId}` : 'No workflow selected')

  return (
    <main className="w-full min-h-screen pt-12 pl-64 px-4 bg-gray-50 dark:bg-gray-900 dark:text-gray-100 transition-colors">
      <div className="w-full mx-auto flex border rounded-xl shadow bg-white dark:bg-gray-950 dark:border-gray-800 overflow-hidden min-h-[70vh] text-base">
        <aside className="w-64 border-r bg-gray-50 dark:bg-gray-900 dark:border-gray-800 flex flex-col">
          <div className="px-4 py-3 border-b border-gray-200 dark:border-gray-800">
            <h2 className="text-base font-semibold text-gray-700 dark:text-gray-200">Workflows</h2>
          </div>

          <div className="flex-1 overflow-y-auto">
            {loadingList ? (
              <div className="p-4 text-sm text-gray-500 dark:text-gray-400">Loading workflows...</div>
            ) : error ? (
              <div className="p-4 text-sm text-gray-500 dark:text-gray-400">{error}</div>
            ) : workflows.length === 0 ? (
              <div className="p-4 text-sm text-gray-500 dark:text-gray-400">No workflows found.</div>
            ) : (
              <ul className="py-2">
                {workflows.map(w => {
                  const id = w.WorkflowID ?? w.ID ?? w.id
                  const label = w.Name || `Workflow #${id}`
                  return (
                    <li key={id}>
                      <button
                        type="button"
                        onClick={() => { setSelectedId(id); setMode('view') }}
                        className={
                          'w-full text-left px-4 py-2 text-base transition ' +
                          (id === selectedId
                            ? 'bg-blue-600 text-white'
                            : 'hover:bg-gray-100 text-gray-700 dark:text-gray-200 dark:hover:bg-gray-800')
                        }
                      >
                        {label}
                      </button>
                    </li>
                  )
                })}
              </ul>
            )}
          </div>

          <div className="px-4 py-3 border-t border-gray-200 dark:border-gray-800">
            <button
              type="button"
              onClick={onCreate}
              className="w-full flex items-center justify-center gap-1 text-sm font-medium px-3 py-2 rounded-md bg-blue-600 text-white hover:bg-blue-700 transition"
            >
              <span>+</span>
              <span>Create workflow</span>
            </button>
          </div>
        </aside>

        <section className="flex-1 p-6">
          <div className="flex items-start gap-3 justify-between">
            <h1 className="text-2xl font-semibold mb-4 text-gray-800 dark:text-gray-100">{title}</h1>

            {!isCreate && selectedId != null ? (
              <button
                type="button"
                onClick={openLogs}
                className="mt-0.5 inline-flex items-center gap-2 px-3 py-2 rounded-lg text-sm font-medium
                           border border-gray-300 text-gray-700 hover:bg-gray-100 transition
                           dark:border-gray-700 dark:text-gray-200 dark:hover:bg-gray-900"
                title="View workflow logs"
              >
                <span>Logs</span>
              </button>
            ) : null}
          </div>

          {(isCreate || isEdit) ? (
            <form onSubmit={onSave} className="space-y-4 max-w-xl">
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-200">Name</label>
                <input
                  type="text"
                  value={name}
                  onChange={e => setName(e.target.value)}
                  className="mt-1 w-full px-3 py-2 border rounded-lg outline-none focus:border-indigo-600 transition
                             bg-white text-gray-900 border-gray-300
                             dark:bg-gray-900 dark:text-gray-100 dark:border-gray-700"
                  required
                />
              </div>

              <div className="flex items-center gap-2">
                <input id="active" type="checkbox" checked={active} onChange={e => setActive(e.target.checked)} />
                <label htmlFor="active" className="text-sm text-gray-700 dark:text-gray-200">Active</label>
              </div>

              <SelectWithDetails
                label="Action"
                required
                loading={loadingActions}
                options={actions}
                value={actionName}
                setValue={setActionName}
                detailsByName={actionDetailsByName}
                detailsLoading={loadingActionDetails}
                selectedDetails={selectedActionDetails}
                emptyOptionsText="No actions available"
              />
              <ParamsEditor title="Action" details={selectedActionDetails} values={actionParams} setKV={kvAction} />

              <SelectWithDetails
                label="Modifier"
                required={false}
                optionalLabel="No modifier"
                loading={loadingModifiers}
                options={modifiers}
                value={modifierName}
                setValue={setModifierName}
                detailsByName={modifierDetailsByName}
                detailsLoading={loadingModifierDetails}
                selectedDetails={selectedModifierDetails}
              />
              <ParamsEditor title="Modifier" details={selectedModifierDetails} values={modifierParams} setKV={kvModifier} />

              <SelectWithDetails
                label="Reaction"
                required
                loading={loadingReactions}
                options={reactions}
                value={reactionName}
                setValue={setReactionName}
                detailsByName={reactionDetailsByName}
                detailsLoading={loadingReactionDetails}
                selectedDetails={selectedReactionDetails}
                emptyOptionsText="No reactions available"
              />
              <ParamsEditor title="Reaction" details={selectedReactionDetails} values={reactionParams} setKV={kvReaction} />

              <div className="flex items-center gap-3 mt-4">
                <button
                  type="submit"
                  disabled={saving || loadingDetails}
                  className={
                    'px-4 py-2 rounded-lg text-sm font-medium text-white ' +
                    (saving || loadingDetails
                      ? 'bg-gray-400 cursor-not-allowed'
                      : 'bg-blue-600 hover:bg-blue-700 transition')
                  }
                >
                  {saving ? (isCreate ? 'Creating...' : 'Saving...') : (isCreate ? 'Create workflow' : 'Update workflow')}
                </button>

                <button
                  type="button"
                  onClick={onCancel}
                  className="px-4 py-2 rounded-lg text-sm font-medium border border-gray-300 text-gray-700 hover:bg-gray-100 transition
                             dark:border-gray-700 dark:text-gray-200 dark:hover:bg-gray-800"
                >
                  Cancel
                </button>

                {!isCreate && selectedId != null ? (
                  <button
                    type="button"
                    onClick={onDelete}
                    disabled={saving}
                    className={
                      'ml-auto px-4 py-2 rounded-lg text-sm font-medium text-white ' +
                      (saving ? 'bg-red-300 cursor-not-allowed' : 'bg-red-600 hover:bg-red-700 transition')
                    }
                  >
                    Delete
                  </button>
                ) : null}
              </div>
            </form>
          ) : selectedId == null ? (
            <div className="w-full min-h-[300px] border border-dashed border-gray-300 dark:border-gray-700 rounded-lg flex items-center justify-center text-gray-400 dark:text-gray-500 text-sm">
              Select a workflow on the left to see its details.
            </div>
          ) : (
            <div className="w-full min-h-[300px] border border-gray-200 dark:border-gray-800 rounded-lg p-4 bg-gray-50 dark:bg-gray-900">
              {loadingDetails ? (
                <p className="text-sm text-gray-500 dark:text-gray-400">Loading details...</p>
              ) : (
                <p className="text-sm text-gray-700 dark:text-gray-200">Select this workflow again to edit.</p>
              )}
            </div>
          )}

          {logsOpen ? (
            <div className="fixed inset-0 z-50 flex items-center justify-center px-4" aria-modal="true" role="dialog">
              <button
                type="button"
                onClick={closeLogs}
                className="absolute inset-0 bg-black/40"
                aria-label="Close logs modal"
              />

              <div className="relative w-full max-w-3xl max-h-[80vh] rounded-xl border border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-950 shadow-xl overflow-hidden">
                <div className="flex items-center justify-between px-4 py-3 border-b border-gray-200 dark:border-gray-800">
                  <div>
                    <p className="text-sm font-semibold text-gray-800 dark:text-gray-100">Workflow logs</p>
                    <p className="text-xs text-gray-500 dark:text-gray-400">WorkflowID: {selectedId}</p>
                  </div>

                  <div className="flex items-center gap-2">
                    <button
                      type="button"
                      onClick={() => loadLogs(selectedId)}
                      disabled={logsLoading}
                      className={
                        'px-3 py-2 rounded-lg text-sm font-medium border transition ' +
                        (logsLoading
                          ? 'border-gray-300 text-gray-400 cursor-not-allowed dark:border-gray-800'
                          : 'border-gray-300 text-gray-700 hover:bg-gray-100 dark:border-gray-700 dark:text-gray-200 dark:hover:bg-gray-900')
                      }
                      title="Refresh"
                    >
                      Refresh
                    </button>
                    <button
                      type="button"
                      onClick={closeLogs}
                      className="px-3 py-2 rounded-lg text-sm font-medium border border-gray-300 text-gray-700 hover:bg-gray-100 transition
                                 dark:border-gray-700 dark:text-gray-200 dark:hover:bg-gray-900"
                      title="Close (Esc)"
                    >
                      Close
                    </button>
                  </div>
                </div>

                <div className="px-4 py-3 border-b border-gray-200 dark:border-gray-800">
                  <input
                    type="text"
                    value={logsQuery}
                    onChange={(e) => setLogsQuery(e.target.value)}
                    placeholder="Search in logs (message, type, timestamp)..."
                    className="w-full px-3 py-2 rounded-lg border border-gray-300 bg-white text-gray-900 outline-none focus:border-indigo-600 transition
                               dark:border-gray-700 dark:bg-gray-900 dark:text-gray-100"
                  />
                </div>

                <div className="p-4 overflow-y-auto max-h-[calc(80vh-120px)]">
                  {logsLoading ? (
                    <p className="text-sm text-gray-500 dark:text-gray-400">Loading logs...</p>
                  ) : logsError ? (
                    <div className="text-sm text-red-600 dark:text-red-400">
                      {logsError}
                      <div className="mt-2">
                        <button
                          type="button"
                          onClick={() => loadLogs(selectedId)}
                          className="px-3 py-2 rounded-lg text-sm font-medium bg-blue-600 text-white hover:bg-blue-700 transition"
                        >
                          Retry
                        </button>
                      </div>
                    </div>
                  ) : filteredLogs.length === 0 ? (
                    <p className="text-sm text-gray-500 dark:text-gray-400">No logs found.</p>
                  ) : (
                    <ul className="space-y-2">
                      {filteredLogs.map((l, idx) => {
                        const msg = l?.Message ?? l?.message ?? ''
                        const typ = l?.Type ?? l?.type ?? ''
                        const tsRaw = l?.Timestamp ?? l?.timestamp ?? ''
                        const ts = tsRaw ? new Date(tsRaw).toLocaleString() : ''
                        return (
                          <li
                            key={`${tsRaw}-${typ}-${idx}`}
                            className="rounded-lg border border-gray-200 dark:border-gray-800 bg-gray-50 dark:bg-gray-900 p-3"
                          >
                            <div className="flex flex-wrap items-center gap-x-3 gap-y-1">
                              <span className="text-xs font-semibold uppercase tracking-wide text-gray-600 dark:text-gray-300">
                                {typ || 'LOG'}
                              </span>
                              {ts ? (
                                <span className="text-xs text-gray-500 dark:text-gray-400">{ts}</span>
                              ) : null}
                            </div>
                            <p className="mt-2 text-sm text-gray-800 dark:text-gray-100 whitespace-pre-wrap break-words">
                              {String(msg)}
                            </p>
                          </li>
                        )
                      })}
                    </ul>
                  )}
                </div>
              </div>
            </div>
          ) : null}

          <Toast
            type={toast.type}
            message={toast.message}
            onClose={() => setToast({ type: 'success', message: '' })}
          />
        </section>
      </div>
    </main>
  )
}

export default Workflows
