const API_ROOT = process.env.REACT_APP_API_URL
const TOKEN_COOKIE_NAME = 'auth_token'

function getTokenFromCookie() {
    const cookies = document.cookie.split(';').map(c => c.trim())
    for (const c of cookies) {
        if (c.startsWith(`${TOKEN_COOKIE_NAME}=`)) {
            return decodeURIComponent(c.substring(TOKEN_COOKIE_NAME.length + 1))
        }
    }
    return null
}

async function request(path, options = {}) {
    if (!API_ROOT) {
        throw new Error('REACT_APP_API_URL is not defined')
    }

    const token = getTokenFromCookie()

    const headers = {
        Accept: 'application/json',
        ...(options.headers || {}),
    }

    if (options.body && !headers['Content-Type']) {
        headers['Content-Type'] = 'application/json'
    }

    if (token) {
        headers.Authorization = `Bearer ${token}`
    }

    const res = await fetch(`${API_ROOT}${path}`, {
        ...options,
        headers,
    })

    const text = await res.text()
    let data
    try {
        data = text ? JSON.parse(text) : {}
    } catch {
        data = { message: text }
    }

    if (!res.ok) {
        const msg = data.message || data.error || `Erreur HTTP ${res.status}`
        throw new Error(msg)
    }

    return data
}
export function apiSignIn(email, password) {
    return request('/auth/sign-in', {
        method: 'POST',
        body: JSON.stringify({ email, password }),
    })
}

export function apiSignUp(email, password) {
    return request('/auth/signup', {
        method: 'POST',
        body: JSON.stringify({ email, password }),
    })
}

export function apiGithubInit(token) {
    return request('/providers/github/auth/init?platform=web', {
        method: 'GET',
    })
}

export function apiGithubCheck() {
    return request('/providers/github/auth/check', {
        method: 'GET',
    })
}

export function apiGetWorkflows() {
    return request('/workflow/', {
        method: 'GET',
    })
}

export function apiCreateWorkflow(body) {
    return request('/workflow/', {
        method: 'POST',
        body: JSON.stringify(body),
    })
}

export function apiGetWorkflow(id) {
    return request(`/workflow/${id}`, {
        method: 'GET',
    })
}

export function apiGetActions() {
    return request('/action/', {
        method: 'GET'
    })
}

export function apiGetActionDetails(name) {
    return request(`/action/${encodeURIComponent(name)}`, {
        method: 'GET',
    })
}

export function apiGetModifiers() {
    return request('/modifiers/', { method: 'GET' })
}

export function apiGetModifierDetails(name) {
    return request(`/modifiers/${encodeURIComponent(name)}`, { method: 'GET' })
}

export function apiGetReactions() {
    return request('/reaction/', { method: 'GET' })
}

export function apiGetReactionDetails(name) {
    return request(`/reaction/${encodeURIComponent(name)}`, { method: 'GET' })
}

export function apiCheckWorkflow(body) {
    return request('/workflow/check', {
        method: 'POST',
        body: JSON.stringify(body),
    })
}

export function apiPatchWorkflow(id, body) {
    return request(`/workflow/${id}`, {
        method: 'PATCH',
        body: JSON.stringify(body),
    })
}

export function apiDeleteWorkflow(id) {
    return request(`/workflow/${id}`, {
        method: 'DELETE'
    })
}

export function apiGetWorkflowLogs(id) {
    return request(`/logs/workflow/${id}`, { method: 'GET' })
}

export function apiGoogleInit() {
    return request('/providers/google/auth/init?platform=web', {
        method: 'GET',
    })
}

export function apiGoogleCheck() {
    return request('/providers/google/auth/check', {
        method: 'GET',
    })
}

export function apiNotionInit() {
    return request('/providers/notion/auth/init?platform=web', {
        method: 'GET',
    })
}

export function apiNotionCheck() {
    return request('/providers/notion/auth/check', {
        method: 'GET',
    })
}