import React, { createContext, useContext, useEffect, useState } from 'react'
import { apiSignIn, apiSignUp } from '../../api/auth'

const AuthContext = createContext(null)

export const AuthProvider = ({ children }) => {
  const [token, setToken] = useState(null)
  const [email, setEmail] = useState(null)
  const [isHydrated, setIsHydrated] = useState(false)

  useEffect(() => {
    const storedToken = localStorage.getItem('auth_token')
    const storedEmail = localStorage.getItem('auth_email')
    if (storedToken) setToken(storedToken)
    if (storedEmail) setEmail(storedEmail)
    setIsHydrated(true)
  }, [])

  const login = async (emailArg, password) => {
    const data = await apiSignIn(emailArg, password)
    const newToken = data.token
    const newEmail = emailArg

    setToken(newToken)
    setEmail(newEmail)
    localStorage.setItem('auth_token', newToken)
    localStorage.setItem('auth_email', newEmail)
  }

  const register = async (emailArg, password) => {
    const data = await apiSignUp(emailArg, password)
    const newToken = data.token
    const newEmail = emailArg

    setToken(newToken)
    setEmail(newEmail)
    localStorage.setItem('auth_token', newToken)
    localStorage.setItem('auth_email', newEmail)
  }

  const logout = () => {
    setToken(null)
    setEmail(null)
    localStorage.removeItem('auth_token')
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
