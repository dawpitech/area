import React, { createContext, useContext, useEffect, useState } from 'react'
import { apiSignIn, apiSignUp } from '../../api/auth'
const TOKEN_COOKIE_NAME = 'auth_token'
const TOKEN_TTL_SECONDS = 24 * 60 * 60

function setTokenCookie(token) {
  document.cookie = `${TOKEN_COOKIE_NAME}=${encodeURIComponent(
    token
  )}; Path=/; Max-Age=${TOKEN_TTL_SECONDS}; SameSite=Lax`
}

function clearTokenCookie() {
  document.cookie = `${TOKEN_COOKIE_NAME}=; Path=/; Max-Age=0; SameSite=Lax`
}

function getTokenFromCookie() {
  const cookies = document.cookie.split(';').map(c => c.trim())
  for (const c of cookies) {
    if (c.startsWith(`${TOKEN_COOKIE_NAME}=`)) {
      return decodeURIComponent(c.substring(TOKEN_COOKIE_NAME.length + 1))
    }
  }
  return null
}


const AuthContext = createContext(null)

export const AuthProvider = ({ children }) => {
  const [token, setToken] = useState(null)
  const [email, setEmail] = useState(null)
  const [isHydrated, setIsHydrated] = useState(false)

  useEffect(() => {
    const cookieToken = getTokenFromCookie()
    const storedEmail = localStorage.getItem('auth_email')

    if (cookieToken) {
      setToken(cookieToken)
    }
    if (storedEmail) {
      setEmail(storedEmail)
    }

    setIsHydrated(true)
  }, [])

  const login = async (emailArg, password) => {
    const data = await apiSignIn(emailArg, password)
    const newToken = data.token
    const newEmail = emailArg

    setToken(newToken)
    setEmail(newEmail)

    setTokenCookie(newToken)
    localStorage.setItem('auth_email', newEmail)
  }

  const register = async (emailArg, password) => {
    const data = await apiSignUp(emailArg, password)
    const newToken = data.token
    const newEmail = emailArg

    setToken(newToken)
    setEmail(newEmail)

    setTokenCookie(newToken)
    localStorage.setItem('auth_email', newEmail)
  }

  const logout = () => {
    setToken(null)
    setEmail(null)
    clearTokenCookie()
    localStorage.removeItem('auth_email')
  }

  const value = {
    userLoggedIn: !!token,
    token,
    email,
    login,
    register,
    logout,
  }

  if (!isHydrated) return null

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export const useAuth = () => {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within an AuthProvider')
  return ctx
}