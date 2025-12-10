import React, { useEffect, useState } from 'react'
import { apiGetWorkflows, apiCreateWorkflow } from '../../api/auth'
import { useAuth } from '../../contexts/authContext'

const Workflows = () => {
  const { token } = useAuth()
  const [workflows, setWorkflows] = useState([])
  const [selectedId, setSelectedId] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  // état création
  const [isCreatingMode, setIsCreatingMode] = useState(false)
  const [creating, setCreating] = useState(false)
  const [actionName, setActionName] = useState('')
  const [reactionName, setReactionName] = useState('')
  const [actionParamsText, setActionParamsText] = useState('')
  const [reactionParamsText, setReactionParamsText] = useState('')

  useEffect(() => {
    if (!token) {
      setLoading(false)
      return
    }

    const fetchWorkflows = async () => {
      try {
        setLoading(true)
        setError('')
        const data = await apiGetWorkflows()
        const list = Array.isArray(data) ? data : []
        setWorkflows(list)
        if (list.length > 0) {
          const firstId = list[0].ID ?? list[0].id ?? 0
          setSelectedId(firstId)
        }
      } catch (err) {
        console.error(err)
        setError(err.message || 'Failed to load workflows')
      } finally {
        setLoading(false)
      }
    }

    fetchWorkflows()
  }, [token])

  const selectedWorkflow =
    workflows.find(w => (w.ID ?? w.id) === selectedId) || null

  const resetCreateForm = () => {
    setActionName('')
    setReactionName('')
    setActionParamsText('')
    setReactionParamsText('')
  }

  const handleClickCreateMode = () => {
    setIsCreatingMode(true)
    resetCreateForm()
  }

  const handleCancelCreate = () => {
    setIsCreatingMode(false)
    resetCreateForm()
  }

  const handleSubmitCreate = async (e) => {
    e.preventDefault()
    if (!token) {
      alert('You must be logged in to create a workflow.')
      return
    }
    if (!actionName.trim() || !reactionName.trim()) {
      alert('Action name and reaction name are required.')
      return
    }
    if (creating) return

    setCreating(true)
    setError('')

    const actionParams = actionParamsText
      .split('\n')
      .map(s => s.trim())
      .filter(Boolean)

    const reactionParams = reactionParamsText
      .split('\n')
      .map(s => s.trim())
      .filter(Boolean)

    const payload = {
      ActionName: actionName.trim(),
      ActionParameters: actionParams,
      ReactionName: reactionName.trim(),
      ReactionParameters: reactionParams,
    }

    try {
      const created = await apiCreateWorkflow(payload)

      setWorkflows(prev => [...prev, created])
      const newId = created.ID ?? created.id
      if (newId !== undefined) {
        setSelectedId(newId)
      }

      setIsCreatingMode(false)
      resetCreateForm()
    } catch (err) {
      console.error(err)
      setError(err.message || 'Failed to create workflow')
      alert('Erreur lors de la création du workflow (voir console).')
    } finally {
      setCreating(false)
    }
  }

  return (
    <main className="w-full min-h-screen pt-16 px-4 bg-gray-50">
      <div className="w-full mx-auto flex border rounded-xl shadow bg-white overflow-hidden min-h-[70vh] text-base">
        <aside className="w-64 border-r bg-gray-50 flex flex-col">
          <div className="px-4 py-3 border-b">
            <h2 className="text-base font-semibold text-gray-700">
              Workflows
            </h2>
          </div>

          <div className="flex-1 overflow-y-auto">
            {loading && (
              <div className="p-4 text-sm text-gray-500">
                Loading workflows...
              </div>
            )}

            {error && !loading && (
              <div className="p-4 text-sm text-gray-500">
                {error}
              </div>
            )}

            {!loading && !error && workflows.length === 0 && (
              <div className="p-4 text-sm text-gray-500">
                No workflows found.
              </div>
            )}

            {!loading && !error && workflows.length > 0 && (
              <ul className="py-2">
                {workflows.map(workflow => {
                  const id = workflow.ID ?? workflow.id
                  const name =
                    workflow.Name ||
                    workflow.ActionName ||
                    `Workflow #${id}`

                  return (
                    <li key={id}>
                      <button
                        type="button"
                        onClick={() => {
                          setSelectedId(id)
                          setIsCreatingMode(false)
                        }}
                        className={
                          'w-full text-left px-4 py-2 text-base transition ' +
                          (id === selectedId
                            ? 'bg-blue-600 text-white'
                            : 'hover:bg-gray-100 text-gray-700')
                        }
                      >
                        {name}
                      </button>
                    </li>
                  )
                })}
              </ul>
            )}
          </div>

          <div className="px-4 py-3 border-t">
            <button
              type="button"
              onClick={handleClickCreateMode}
              className="w-full flex items-center justify-center gap-1 text-sm font-medium px-3 py-2 rounded-md bg-blue-600 text-white hover:bg-blue-700 transition"
            >
              <span>+</span>
              <span>Create workflow</span>
            </button>
          </div>
        </aside>

        <section className="flex-1 p-6">
          <h1 className="text-2xl font-semibold mb-4 text-gray-800">
            {isCreatingMode
              ? 'Create a new workflow'
              : selectedWorkflow
                ? (selectedWorkflow.Name ||
                   selectedWorkflow.ActionName ||
                   `Workflow #${selectedWorkflow.ID ?? selectedWorkflow.id}`)
                : 'No workflow selected'}
          </h1>

          {isCreatingMode ? (
            <form
              onSubmit={handleSubmitCreate}
              className="space-y-4 max-w-xl"
            >
              <div>
                <label className="block text-sm font-medium text-gray-700">
                  Action name
                </label>
                <input
                  type="text"
                  value={actionName}
                  onChange={e => setActionName(e.target.value)}
                  className="mt-1 w-full px-3 py-2 border rounded-lg outline-none focus:border-indigo-600 transition"
                  placeholder="Ex: timer_cron_job"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700">
                  Action parameters
                  <span className="text-xs text-gray-400"> (one per line)</span>
                </label>
                <textarea
                  value={actionParamsText}
                  onChange={e => setActionParamsText(e.target.value)}
                  className="mt-1 w-full px-3 py-2 border rounded-lg outline-none focus:border-indigo-600 transition min-h-[80px]"
                  placeholder={'Ex:\n* * * * *'}
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700">
                  Reaction name
                </label>
                <input
                  type="text"
                  value={reactionName}
                  onChange={e => setReactionName(e.target.value)}
                  className="mt-1 w-full px-3 py-2 border rounded-lg outline-none focus:border-indigo-600 transition"
                  placeholder="Ex: github_create_issue"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700">
                  Reaction parameters
                  <span className="text-xs text-gray-400"> (one per line)</span>
                </label>
                <textarea
                  value={reactionParamsText}
                  onChange={e => setReactionParamsText(e.target.value)}
                  className="mt-1 w-full px-3 py-2 border rounded-lg outline-none focus:border-indigo-600 transition min-h-[80px]"
                  placeholder={'Ex:\ndawpitech/test-thingy'}
                />
              </div>

              <div className="flex items-center gap-3 mt-4">
                <button
                  type="submit"
                  disabled={creating}
                  className={
                    'px-4 py-2 rounded-lg text-sm font-medium text-white ' +
                    (creating
                      ? 'bg-gray-400 cursor-not-allowed'
                      : 'bg-blue-600 hover:bg-blue-700 transition')
                  }
                >
                  {creating ? 'Creating...' : 'Create workflow'}
                </button>

                <button
                  type="button"
                  onClick={handleCancelCreate}
                  className="px-4 py-2 rounded-lg text-sm font-medium border border-gray-300 text-gray-700 hover:bg-gray-100 transition"
                >
                  Cancel
                </button>
              </div>
            </form>
          ) : selectedWorkflow ? (
            <div className="w-full min-h-[300px] border border-gray-200 rounded-lg p-4 bg-gray-50">
              <div className="mb-4">
                <h2 className="text-sm font-semibold text-gray-700 uppercase tracking-wide mb-1">
                  Action
                </h2>
                <p className="text-sm text-gray-800">
                  <span className="font-medium">Name:</span>{' '}
                  {selectedWorkflow.ActionName || '—'}
                </p>

                <p className="text-sm text-gray-800 mt-1">
                  <span className="font-medium">Parameters:</span>
                </p>
                {Array.isArray(selectedWorkflow.ActionParameters) &&
                selectedWorkflow.ActionParameters.length > 0 ? (
                  <ul className="list-disc list-inside text-sm text-gray-700 mt-1">
                    {selectedWorkflow.ActionParameters.map((p, idx) => (
                      <li key={idx}>{p}</li>
                    ))}
                  </ul>
                ) : (
                  <p className="text-xs text-gray-400 mt-1">
                    No action parameters.
                  </p>
                )}
              </div>
              <div className="mb-4">
                <h2 className="text-sm font-semibold text-gray-700 uppercase tracking-wide mb-1">
                  Reaction
                </h2>
                <p className="text-sm text-gray-800">
                  <span className="font-medium">Name:</span>{' '}
                  {selectedWorkflow.ReactionName || '—'}
                </p>

                <p className="text-sm text-gray-800 mt-1">
                  <span className="font-medium">Parameters:</span>
                </p>
                {Array.isArray(selectedWorkflow.ReactionParameters) &&
                selectedWorkflow.ReactionParameters.length > 0 ? (
                  <ul className="list-disc list-inside text-sm text-gray-700 mt-1">
                    {selectedWorkflow.ReactionParameters.map((p, idx) => (
                      <li key={idx}>{p}</li>
                    ))}
                  </ul>
                ) : (
                  <p className="text-xs text-gray-400 mt-1">
                    No reaction parameters.
                  </p>
                )}
              </div>
              <div className="border-t pt-3 mt-2 text-xs text-gray-500 space-y-1">
                <p>
                  <span className="font-medium">ID:</span>{' '}
                  {selectedWorkflow.ID ?? selectedWorkflow.id ?? '—'}
                </p>
                <p>
                  <span className="font-medium">OwnerUserID:</span>{' '}
                  {selectedWorkflow.OwnerUserID ?? '—'}
                </p>
                <p>
                  <span className="font-medium">Created at:</span>{' '}
                  {selectedWorkflow.CreatedAt || '—'}
                </p>
                <p>
                  <span className="font-medium">Updated at:</span>{' '}
                  {selectedWorkflow.UpdatedAt || '—'}
                </p>
              </div>
            </div>
          ) : (
            <div className="w-full min-h-[300px] border border-dashed border-gray-300 rounded-lg flex items-center justify-center text-gray-400 text-sm">
              Select a workflow on the left to see its details.
            </div>
          )}
        </section>
      </div>
    </main>
  )
}

export default Workflows
