const API_ROOT = 'http://localhost:8080'

async function request(path, options = {}) {
    const res = await fetch(`${API_ROOT}${path}`, {
        headers: {
            'Content-Type': 'application/json',
            ...(options.headers || {}),
        },
        ...options,
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
