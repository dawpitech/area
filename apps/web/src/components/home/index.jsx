import React, { useState, useEffect } from 'react'
import { useAuth } from '../../contexts/authContext'
import { apiGithubInit, apiGithubCheck } from '../../api/auth'
import { FaGithub, FaDiscord, FaSteam, FaInstagram } from 'react-icons/fa'
import { FiEye, FiEyeOff } from 'react-icons/fi'

const Home = () => {
  const { email, token } = useAuth()

  const [showPassword, setShowPassword] = useState(false)
  const displayedPassword = showPassword ? 'not-stored-client-side' : '************'

  const [githubConnected, setGithubConnected] = useState(false)
  const [checkingGithub, setCheckingGithub] = useState(false)

  useEffect(() => {
    if (!token) {
      setGithubConnected(false)
      return
    }

    const check = async () => {
      try {
        setCheckingGithub(true)
        const data = await apiGithubCheck()
        setGithubConnected(!!data.is_connected)
      } catch (err) {
        console.error('GitHub check error:', err)
        setGithubConnected(false)
      } finally {
        setCheckingGithub(false)
      }
    }

    check()
  }, [token])

  const handleConnect = async (provider) => {
    switch (provider) {
      case 'github': {
        if (!token) {
          alert('You must be logged in to connect GitHub.')
          return
        }
        if (githubConnected) return

        try {
          const data = await apiGithubInit()
          if (data && data.redirect_to) {
            window.open(data.redirect_to, '_blank', 'noopener,noreferrer')
          } else {
            console.error('Unexpected GitHub init response:', data)
            alert('Erreur: missing redirect URL')
          }
        } catch (err) {
          console.error(err)
          alert('Erreur lors de la connexion GitHub (voir console).')
        }
        break
      }

      case 'discord':
      case 'steam':
      case 'instagram':
      default:
        alert(`Connexion ${provider} pas encore implémentée`)
        break
    }
  }

  return (
    <main className="w-full min-h-screen pt-16 px-4 bg-gray-50">
      <div className="max-w-5xl mx-auto">
        <h1 className="text-2xl md:text-3xl font-bold mb-6 text-gray-800 text-center">
          {email ? (
            <>
              Hello <span className="text-indigo-600">{email}</span>, you are now logged in.
            </>
          ) : (
            <>Hello, you are now logged in.</>
          )}
        </h1>

        <div className="bg-white rounded-xl shadow-lg border px-6 py-6 mb-6">
          <h2 className="text-lg font-semibold text-gray-800 mb-4">
            Account information
          </h2>

          <div className="space-y-4">
            <div>
              <label className="text-sm font-medium text-gray-600">
                Email
              </label>
              <input
                type="email"
                value={email || ''}
                readOnly
                className="mt-1 w-full px-3 py-2 text-gray-700 bg-gray-50 border rounded-lg outline-none focus:border-indigo-600 transition"
              />
            </div>

            <div>
              <label className="text-sm font-medium text-gray-600">
                Password
              </label>
              <div className="relative mt-1">
                <input
                  type={showPassword ? 'text' : 'password'}
                  value={displayedPassword}
                  readOnly
                  className="w-full px-3 py-2 pr-10 text-gray-700 bg-gray-50 border rounded-lg outline-none focus:border-indigo-600 transition"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(prev => !prev)}
                  className="absolute inset-y-0 right-2 my-auto flex items-center justify-center text-gray-500 hover:text-indigo-600 transition"
                  aria-label={showPassword ? 'Masquer le mot de passe' : 'Afficher le mot de passe'}
                >
                  {showPassword ? <FiEyeOff size={18} /> : <FiEye size={18} />}
                </button>
              </div>
              <p className="text-xs text-gray-400 mt-1">
                For security reasons, the real password is not stored on the client.
              </p>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-xl shadow-lg border px-6 py-6">
          <h2 className="text-lg font-semibold mb-2 text-gray-800 text-center">
            Connect your accounts
          </h2>
          <p className="text-sm text-gray-500 mb-6 text-center">
            Link your favorite platforms to unlock more features.
          </p>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <button
              type="button"
              onClick={() => handleConnect('github')}
              disabled={githubConnected || checkingGithub}
              className={
                'flex items-center justify-center gap-2 px-4 py-3 rounded-lg border shadow-sm transition duration-300 ease-out ' +
                (githubConnected
                  ? 'bg-green-600 text-white cursor-default'
                  : 'bg-gray-900 text-white hover:-translate-y-0.5 hover:shadow-xl hover:ring-2 hover:ring-gray-400/70') +
                (checkingGithub ? ' opacity-70 cursor-wait' : '')
              }
            >
              <FaGithub size={18} />
              <span className="font-medium text-sm">
                {checkingGithub
                  ? 'Checking...'
                  : githubConnected
                    ? 'Connected to Github'
                    : 'Connect Github'}
              </span>
            </button>
            <button
              type="button"
              onClick={() => handleConnect('discord')}
              className="flex items-center justify-center gap-2 px-4 py-3 rounded-lg border shadow-sm 
                         bg-[#5865F2] text-white
                         transition duration-300 ease-out
                         hover:-translate-y-0.5 hover:shadow-xl hover:ring-2 hover:ring-[#5865F2]/70"
            >
              <FaDiscord size={18} />
              <span className="font-medium text-sm">Connect Discord</span>
            </button>
            <button
              type="button"
              onClick={() => handleConnect('steam')}
              className="flex items-center justify-center gap-2 px-4 py-3 rounded-lg border shadow-sm
                         bg-[#171a21] text-white
                         transition duration-300 ease-out
                         hover:-translate-y-0.5 hover:shadow-xl hover:ring-2 hover:ring-[#66c0f4]/70"
            >
              <FaSteam size={18} />
              <span className="font-medium text-sm">Connect Steam</span>
            </button>
            <button
              type="button"
              onClick={() => handleConnect('instagram')}
              className="flex items-center justify-center gap-2 px-4 py-3 rounded-lg border shadow-sm
                         bg-gradient-to-r from-[#f58529] via-[#dd2a7b] to-[#515bd4] text-white
                         transition duration-300 ease-out
                         hover:-translate-y-0.5 hover:shadow-xl hover:ring-2 hover:ring-[#dd2a7b]/70"
            >
              <FaInstagram size={18} />
              <span className="font-medium text-sm">Connect Instagram</span>
            </button>
          </div>
        </div>
      </div>
    </main>
  )
}

export default Home
