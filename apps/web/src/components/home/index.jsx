import React, { useState, useEffect } from 'react'
import { useAuth } from '../../contexts/authContext'
import {
  apiGithubInit,
  apiGithubCheck,
  apiGoogleInit,
  apiGoogleCheck,
} from '../../api/auth'
import { FaGithub } from 'react-icons/fa'
import { FcGoogle } from 'react-icons/fc'
import { FiEye, FiEyeOff } from 'react-icons/fi'

const Home = () => {
  const { email, token } = useAuth()

  const [showPassword, setShowPassword] = useState(false)
  const displayedPassword = showPassword ? 'not-stored-client-side' : '************'

  const [githubConnected, setGithubConnected] = useState(false)
  const [checkingGithub, setCheckingGithub] = useState(false)

  const [googleConnected, setGoogleConnected] = useState(false)
  const [checkingGoogle, setCheckingGoogle] = useState(false)

  const checkProviders = async () => {
    if (!token) {
      setGithubConnected(false)
      setGoogleConnected(false)
      return
    }

    try {
      setCheckingGithub(true)
      setCheckingGoogle(true)

      const [gh, gg] = await Promise.allSettled([apiGithubCheck(), apiGoogleCheck()])

      setGithubConnected(gh.status === 'fulfilled' ? !!gh.value?.is_connected : false)
      setGoogleConnected(gg.status === 'fulfilled' ? !!gg.value?.is_connected : false)
    } catch (err) {
      console.error('Provider check error:', err)
      setGithubConnected(false)
      setGoogleConnected(false)
    } finally {
      setCheckingGithub(false)
      setCheckingGoogle(false)
    }
  }

  useEffect(() => {
    checkProviders()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token])

  useEffect(() => {
    const onFocus = () => checkProviders()
    window.addEventListener('focus', onFocus)
    return () => window.removeEventListener('focus', onFocus)
    // eslint-disable-next-line react-hooks/exhaustive-deps
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
          alert('Error during GitHub login (see console).')
        }
        break
      }

      case 'google': {
        if (!token) {
          alert('You must be logged in to connect Google.')
          return
        }
        if (googleConnected) return

        try {
          const data = await apiGoogleInit()
          if (data && data.redirect_to) {
            window.open(data.redirect_to, '_blank', 'noopener,noreferrer')
          } else {
            console.error('Unexpected Google init response:', data)
            alert('Erreur: missing redirect URL')
          }
        } catch (err) {
          console.error(err)
          alert('Error during google login (see console).')
        }
        break
      }

      default:
        alert('Provider not yet implemented')
        break
    }
  }

  return (
    <main className="w-full min-h-screen pt-12 pl-64 px-4 bg-gray-50 dark:bg-gray-900 dark:text-gray-100 transition-colors">
      <div className="max-w-5xl mx-auto">
        <h1 className="text-2xl md:text-3xl font-bold mb-6 text-gray-800 dark:text-gray-100 text-center">
          {email ? (
            <>
              Hello <span className="text-indigo-600 dark:text-indigo-400">{email}</span>, you are now logged in.
            </>
          ) : (
            <>Hello, you are now logged in.</>
          )}
        </h1>

        <div className="bg-white dark:bg-gray-950 rounded-xl shadow-lg border border-gray-200 dark:border-gray-800 px-6 py-6 mb-6">
          <h2 className="text-lg font-semibold text-gray-800 dark:text-gray-100 mb-4">
            Account information
          </h2>

          <div className="space-y-4">
            <div>
              <label className="text-sm font-medium text-gray-600 dark:text-gray-200">
                Email
              </label>
              <input
                type="email"
                value={email || ''}
                readOnly
                className="mt-1 w-full px-3 py-2 border rounded-lg outline-none focus:border-indigo-600 transition
                           bg-gray-50 text-gray-700 border-gray-300
                           dark:bg-gray-900 dark:text-gray-100 dark:border-gray-700"
              />
            </div>

            <div>
              <label className="text-sm font-medium text-gray-600 dark:text-gray-200">
                Password
              </label>
              <div className="relative mt-1">
                <input
                  type={showPassword ? 'text' : 'password'}
                  value={displayedPassword}
                  readOnly
                  className="w-full px-3 py-2 pr-10 border rounded-lg outline-none focus:border-indigo-600 transition
                             bg-gray-50 text-gray-700 border-gray-300
                             dark:bg-gray-900 dark:text-gray-100 dark:border-gray-700"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(prev => !prev)}
                  className="absolute inset-y-0 right-2 my-auto flex items-center justify-center text-gray-500 hover:text-indigo-600 transition
                             dark:text-gray-400 dark:hover:text-indigo-400"
                  aria-label={showPassword ? 'Masquer le mot de passe' : 'Afficher le mot de passe'}
                >
                  {showPassword ? <FiEyeOff size={18} /> : <FiEye size={18} />}
                </button>
              </div>
              <p className="text-xs text-gray-400 dark:text-gray-500 mt-1">
                For security reasons, the real password is not stored on the client.
              </p>
            </div>
          </div>
        </div>

        <div className="bg-white dark:bg-gray-950 rounded-xl shadow-lg border border-gray-200 dark:border-gray-800 px-6 py-6">
          <h2 className="text-lg font-semibold mb-2 text-gray-800 dark:text-gray-100 text-center">
            Connect your accounts
          </h2>
          <p className="text-sm text-gray-500 dark:text-gray-400 mb-6 text-center">
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
                  ? 'bg-green-600 text-white cursor-default border-green-600'
                  : 'bg-gray-900 text-white border-gray-800 hover:-translate-y-0.5 hover:shadow-xl hover:ring-2 hover:ring-gray-400/70') +
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
              onClick={() => handleConnect('google')}
              disabled={googleConnected || checkingGoogle}
              className={
                'flex items-center justify-center gap-2 px-4 py-3 rounded-lg border shadow-sm transition duration-300 ease-out ' +
                (googleConnected
                  ? 'bg-green-600 text-white cursor-default border-green-600'
                  : 'bg-white text-gray-900 border-gray-200 hover:-translate-y-0.5 hover:shadow-xl hover:ring-2 hover:ring-gray-300/70 dark:bg-gray-900 dark:text-gray-100 dark:border-gray-700 dark:hover:ring-gray-600/70') +
                (checkingGoogle ? ' opacity-70 cursor-wait' : '')
              }
            >
              <FcGoogle size={18} />
              <span className="font-medium text-sm">
                {checkingGoogle
                  ? 'Checking...'
                  : googleConnected
                    ? 'Connected to Google'
                    : 'Connect Google'}
              </span>
            </button>
          </div>
        </div>
      </div>
    </main>
  )
}

export default Home
