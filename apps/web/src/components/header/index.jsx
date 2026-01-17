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

  const baseNavItem =
    'block px-3 py-2 rounded-lg text-sm font-medium transition ' +
    'focus:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 ' +
    'focus-visible:ring-blue-500 focus-visible:ring-offset-white ' +
    'dark:focus-visible:ring-offset-gray-950 ' +
    'contrast:focus-visible:ring-yellow-300 contrast:focus-visible:ring-offset-black'

  const inactiveNavItem =
    'text-gray-700 hover:bg-gray-100 ' +
    'dark:text-gray-200 dark:hover:bg-gray-900 ' +
    'contrast:text-white contrast:hover:bg-white/10 contrast:border contrast:border-white'

  const activeNavItem =
    'bg-blue-600 text-white ' +
    'contrast:bg-yellow-300 contrast:text-black contrast:border contrast:border-white'

  const authBtnBase =
    'px-3 py-1 text-sm font-medium rounded-md transition ' +
    'focus:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 ' +
    'focus-visible:ring-blue-500 focus-visible:ring-offset-white ' +
    'dark:focus-visible:ring-offset-gray-900 ' +
    'contrast:focus-visible:ring-yellow-300 contrast:focus-visible:ring-offset-black'

  return (
    <>
      <header
        className="
          fixed top-0 left-0 right-0 z-30 h-12 border-b flex items-center justify-between px-4
          bg-blue-600 text-white border-blue-700
          dark:bg-gray-900 dark:text-gray-100 dark:border-gray-800
          contrast:bg-black contrast:text-white contrast:border-white
        "
      >
        <div className="font-semibold contrast:tracking-wide">
          AREA
        </div>

        <div className="flex-1 flex justify-center">
          {userLoggedIn && email && (
            <span className="text-sm font-medium">
              Logged in as:&nbsp;
              <span className="underline decoration-2 underline-offset-2 contrast:decoration-yellow-300">
                {email}
              </span>
            </span>
          )}
        </div>

        <div>
          {userLoggedIn ? (
            <button
              onClick={handleLogout}
              className={[
                authBtnBase,
                'bg-white text-blue-600 hover:bg-blue-100',
                'dark:bg-gray-100 dark:text-gray-900 dark:hover:bg-white',
                'contrast:bg-yellow-300 contrast:text-black contrast:hover:bg-yellow-200',
              ].join(' ')}
            >
              Disconnect
            </button>
          ) : (
            <Link
              to="/login"
              className={[
                authBtnBase,
                'bg-white text-blue-600 hover:bg-blue-100',
                'dark:bg-gray-100 dark:text-gray-900 dark:hover:bg-white',
                'contrast:bg-yellow-300 contrast:text-black contrast:hover:bg-yellow-200',
              ].join(' ')}
            >
              Connect
            </Link>
          )}
        </div>
      </header>

      {userLoggedIn && (
        <aside
          className="
            fixed top-12 left-0 z-20 h-[calc(100vh-48px)] w-64 border-r
            bg-white border-gray-200
            dark:bg-gray-950 dark:border-gray-800
            contrast:bg-black contrast:border-white
          "
        >
          <nav className="p-3 space-y-2">
            <Link
              to="/home"
              className={
                baseNavItem +
                (isActive('/home') ? ` ${activeNavItem}` : ` ${inactiveNavItem}`)
              }
            >
              My Providers
            </Link>

            <Link
              to="/workflow"
              className={
                baseNavItem +
                (isActive('/workflow') ? ` ${activeNavItem}` : ` ${inactiveNavItem}`)
              }
            >
              My Workflows
            </Link>

            <Link
              to="/settings"
              className={
                baseNavItem +
                (isActive('/settings') ? ` ${activeNavItem}` : ` ${inactiveNavItem}`)
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
