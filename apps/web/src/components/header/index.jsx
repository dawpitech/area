import React from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../contexts/authContext'

const Header = () => {
  const navigate = useNavigate()
  const { userLoggedIn, email, logout } = useAuth()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <nav className="flex flex-row items-center justify-between w-full h-12 fixed top-0 left-0 z-20 px-4 bg-blue-600 text-white border-b">
      <div className="font-semibold">
        logo
      </div>

      <div className="text-sm font-medium text-center flex-1 flex justify-center">
        {userLoggedIn && email && (
          <span>
            Logged in as:&nbsp;
            <span className="underline">{email}</span>
          </span>
        )}
      </div>

      <div>
        {userLoggedIn ? (
          <button
            onClick={handleLogout}
            className="px-3 py-1 text-sm font-medium bg-white text-blue-600 rounded-md hover:bg-blue-100 transition"
          >
            Se déconnecter
          </button>
        ) : (
          <Link
            to="/login"
            className="px-3 py-1 text-sm font-medium bg-white text-blue-600 rounded-md hover:bg-blue-100 transition"
          >
            Se connecter
          </Link>
        )}
      </div>
    </nav>
  )
}

export default Header
