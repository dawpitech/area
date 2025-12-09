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
      {/* Logo à gauche */}
      <div className="font-semibold">
        logo
      </div>

      {/* Zone centrale : visible uniquement si connecté */}
      <div className="flex-1 flex items-center justify-center">
        {userLoggedIn && email && (
          <div className="flex items-center gap-6 text-sm font-medium">
            {/* Homepage */}
            <Link
              to="/home"
              className="underline hover:no-underline hover:opacity-80 transition"
            >
              Homepage
            </Link>

            {/* Texte de connexion */}
            <span className="text-center">
              Logged in as:&nbsp;
              <span className="underline">{email}</span>
            </span>

            {/* Workflows */}
            <Link
              to="/workflow"
              className="underline hover:no-underline hover:opacity-80 transition"
            >
              Workflows
            </Link>
          </div>
        )}
      </div>

      {/* Bouton à droite : login / logout */}
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
