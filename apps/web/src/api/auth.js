const API_ROOT = 'http://localhost:8080'
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
    const token = getTokenFromCookie()

    const headers = {
        'Content-Type': 'application/json',
        ...(options.headers || {}),
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
    return request('/providers/github/auth/init', {
        method: 'GET',
    })
}

export function apiGetWorkflows() {
    return request('/workflows', {
        method: 'GET',
    })
}

export function apiCreateWorkflow(body) {
    return request('/workflows', {
        method: 'POST',
        body: JSON.stringify(body),
    })
}