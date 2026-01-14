import React from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../../contexts/authContext'

const Header = () => {
  const navigate = useNavigate()
  const location = useLocation()
  const { userLoggedIn, email, logout } = useAuth()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const isActive = (path) => location.pathname === path

  return (
    <>
      {/* TOP BAR */}
      <header className="fixed top-0 left-0 right-0 z-30 h-12 border-b flex items-center justify-between px-4
                         bg-blue-600 text-white
                         dark:bg-gray-900 dark:text-gray-100 dark:border-gray-800">
        {/* Logo */}
        <div className="font-semibold">
          AREA
        </div>

        {/* Logged in as */}
        <div className="flex-1 flex justify-center">
          {userLoggedIn && email && (
            <span className="text-sm font-medium">
              Logged in as:&nbsp;<span className="underline">{email}</span>
            </span>
          )}
        </div>

        {/* Login / Logout */}
        <div>
          {userLoggedIn ? (
            <button
              onClick={handleLogout}
              className="px-3 py-1 text-sm font-medium rounded-md transition
                         bg-white text-blue-600 hover:bg-blue-100
                         dark:bg-gray-100 dark:text-gray-900 dark:hover:bg-white"
            >
              Disconnect
            </button>
          ) : (
            <Link
              to="/login"
              className="px-3 py-1 text-sm font-medium rounded-md transition
                         bg-white text-blue-600 hover:bg-blue-100
                         dark:bg-gray-100 dark:text-gray-900 dark:hover:bg-white"
            >
              Connect
            </Link>
          )}
        </div>
      </header>

      {/* SIDEBAR */}
      {userLoggedIn && (
        <aside className="fixed top-12 left-0 z-20 h-[calc(100vh-48px)] w-64 border-r
                          bg-white
                          dark:bg-gray-950 dark:border-gray-800">
          <nav className="p-3 space-y-2">
            <Link
              to="/home"
              className={
                'block px-3 py-2 rounded-lg text-sm font-medium transition ' +
                (isActive('/home')
                  ? 'bg-blue-600 text-white'
                  : 'text-gray-700 hover:bg-gray-100 dark:text-gray-200 dark:hover:bg-gray-900')
              }
            >
              My Providers
            </Link>

            <Link
              to="/workflow"
              className={
                'block px-3 py-2 rounded-lg text-sm font-medium transition ' +
                (isActive('/workflow')
                  ? 'bg-blue-600 text-white'
                  : 'text-gray-700 hover:bg-gray-100 dark:text-gray-200 dark:hover:bg-gray-900')
              }
            >
              My Workflows
            </Link>

            <Link
              to="/settings"
              className={
                'block px-3 py-2 rounded-lg text-sm font-medium transition ' +
                (isActive('/settings')
                  ? 'bg-blue-600 text-white'
                  : 'text-gray-700 hover:bg-gray-100 dark:text-gray-200 dark:hover:bg-gray-900')
              }
            >
              Settings
            </Link>
          </nav>
        </aside>
      )}
    </>
  )
}

export default Header
