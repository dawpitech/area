import React, { useState } from 'react'
import { Navigate, Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../../contexts/authContext'
import { FiEye, FiEyeOff } from 'react-icons/fi'

const Login = () => {
  const { userLoggedIn, login } = useAuth()
  const navigate = useNavigate()

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [isSigningIn, setIsSigningIn] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')
  const [showPassword, setShowPassword] = useState(false)

  const onSubmit = async (e) => {
    e.preventDefault()
    if (isSigningIn) return

    setIsSigningIn(true)
    setErrorMessage('')

    try {
      await login(email, password)
      navigate('/home')
    } catch (err) {
      setErrorMessage(err.message || 'Connexion error')
      setIsSigningIn(false)
    }
  }

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900 contrast:bg-black transition-colors">
      {userLoggedIn && (<Navigate to={'/home'} replace={true} />)}

      <main className="w-full h-screen flex self-center place-content-center place-items-center px-4">
        <div className="w-96 space-y-5 p-4 shadow-xl border rounded-xl
                        bg-white text-gray-600 border-gray-200
                        dark:bg-gray-950 dark:text-gray-200 dark:border-gray-800
                        contrast:bg-black contrast:text-white contrast:border-white">
          <div className="text-center">
            <div className="mt-2">
              <h3 className="text-gray-800 text-xl font-semibold sm:text-2xl dark:text-gray-100 contrast:text-white">
                Welcome Back
              </h3>
            </div>
          </div>

          <form onSubmit={onSubmit} className="space-y-5">
            <div>
              <label className="text-sm font-bold text-gray-600 dark:text-gray-200 contrast:text-white">
                Email
              </label>
              <input
                type="email"
                autoComplete="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full mt-2 px-3 py-2 outline-none border shadow-sm rounded-lg transition duration-300
                           bg-transparent text-gray-700 border-gray-300 focus:border-indigo-600
                           dark:text-gray-100 dark:border-gray-700
                           contrast:text-white contrast:border-white contrast:focus:border-yellow-300
                           focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600
                           contrast:focus-visible:ring-yellow-300 focus-visible:ring-offset-2
                           focus-visible:ring-offset-white dark:focus-visible:ring-offset-gray-900
                           contrast:focus-visible:ring-offset-black"
              />
            </div>

            <div>
              <label className="text-sm font-bold text-gray-600 dark:text-gray-200 contrast:text-white">
                Password
              </label>

              <div className="relative mt-2">
                <input
                  type={showPassword ? 'text' : 'password'}
                  autoComplete="current-password"
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="w-full px-3 py-2 pr-10 outline-none border shadow-sm rounded-lg transition duration-300
                             bg-transparent text-gray-700 border-gray-300 focus:border-indigo-600
                             dark:text-gray-100 dark:border-gray-700
                             contrast:text-white contrast:border-white contrast:focus:border-yellow-300
                             focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600
                             contrast:focus-visible:ring-yellow-300 focus-visible:ring-offset-2
                             focus-visible:ring-offset-white dark:focus-visible:ring-offset-gray-900
                             contrast:focus-visible:ring-offset-black"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(prev => !prev)}
                  className="absolute inset-y-0 right-2 my-auto flex items-center justify-center
                             text-gray-500 hover:text-indigo-600 transition
                             dark:text-gray-400 dark:hover:text-indigo-400
                             contrast:text-white contrast:hover:text-yellow-300
                             focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600
                             contrast:focus-visible:ring-yellow-300 focus-visible:ring-offset-2
                             focus-visible:ring-offset-white dark:focus-visible:ring-offset-gray-900
                             contrast:focus-visible:ring-offset-black"
                  aria-label={showPassword ? 'Masquer le mot de passe' : 'Afficher le mot de passe'}
                >
                  {showPassword ? <FiEyeOff size={18} /> : <FiEye size={18} />}
                </button>
              </div>
            </div>

            {errorMessage && (
              <span className="text-red-600 font-bold contrast:text-yellow-300">
                {errorMessage}
              </span>
            )}

            <button
              type="submit"
              disabled={isSigningIn}
              className={`w-full px-4 py-2 font-medium rounded-lg transition duration-300
                focus:outline-none focus-visible:ring-2 focus-visible:ring-offset-2
                focus-visible:ring-indigo-600 contrast:focus-visible:ring-yellow-300
                focus-visible:ring-offset-white dark:focus-visible:ring-offset-gray-900
                contrast:focus-visible:ring-offset-black
                ${
                  isSigningIn
                    ? 'bg-gray-300 text-gray-600 cursor-not-allowed dark:bg-gray-700 dark:text-gray-300'
                    : 'bg-indigo-600 text-white hover:bg-indigo-700 hover:shadow-xl contrast:bg-yellow-300 contrast:text-black contrast:hover:bg-yellow-200'
                }`}
            >
              {isSigningIn ? 'Signing In...' : 'Sign In'}
            </button>
          </form>

          <p className="text-center text-sm text-gray-600 dark:text-gray-300 contrast:text-white">
            Don't have an account?{' '}
            <Link
              to={'/register'}
              className="font-bold hover:underline contrast:underline contrast:decoration-yellow-300"
            >
              Sign up
            </Link>
          </p>
        </div>
      </main>
    </div>
  )
}

export default Login
