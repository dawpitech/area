import React, { useState } from 'react'
import { Navigate, Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../../contexts/authContext'
import { FiEye, FiEyeOff } from 'react-icons/fi'

const Register = () => {
  const navigate = useNavigate()
  const { userLoggedIn, register } = useAuth()

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setconfirmPassword] = useState('')
  const [isRegistering, setIsRegistering] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')

  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)

  const onSubmit = async (e) => {
    e.preventDefault()

    if (password !== confirmPassword) {
      setErrorMessage('Les mots de passe ne correspondent pas.')
      return
    }

    if (isRegistering) return

    setIsRegistering(true)
    setErrorMessage('')

    try {
      await register(email, password)
      navigate('/login')
    } catch (err) {
      setErrorMessage(err.message || 'Erreur lors de la création du compte')
      setIsRegistering(false)
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
          <div className="text-center mb-6">
            <div className="mt-2">
              <h3 className="text-gray-800 text-xl font-semibold sm:text-2xl dark:text-gray-100 contrast:text-white">
                Create a New Account
              </h3>
              <p className="text-gray-500 text-sm mt-1 dark:text-gray-400 contrast:text-white/80">
                After signing up, you will be sent to the login page.
              </p>
            </div>
          </div>

          <form onSubmit={onSubmit} className="space-y-4">
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
                  disabled={isRegistering}
                  type={showPassword ? 'text' : 'password'}
                  autoComplete="new-password"
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

            <div>
              <label className="text-sm font-bold text-gray-600 dark:text-gray-200 contrast:text-white">
                Confirm Password
              </label>
              <div className="relative mt-2">
                <input
                  disabled={isRegistering}
                  type={showConfirmPassword ? 'text' : 'password'}
                  autoComplete="off"
                  required
                  value={confirmPassword}
                  onChange={(e) => setconfirmPassword(e.target.value)}
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
                  onClick={() => setShowConfirmPassword(prev => !prev)}
                  className="absolute inset-y-0 right-2 my-auto flex items-center justify-center
                             text-gray-500 hover:text-indigo-600 transition
                             dark:text-gray-400 dark:hover:text-indigo-400
                             contrast:text-white contrast:hover:text-yellow-300
                             focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600
                             contrast:focus-visible:ring-yellow-300 focus-visible:ring-offset-2
                             focus-visible:ring-offset-white dark:focus-visible:ring-offset-gray-900
                             contrast:focus-visible:ring-offset-black"
                  aria-label={showConfirmPassword ? 'Masquer la confirmation' : 'Afficher la confirmation'}
                >
                  {showConfirmPassword ? <FiEyeOff size={18} /> : <FiEye size={18} />}
                </button>
              </div>
            </div>

            {errorMessage && (
              <span className="font-bold text-red-600 contrast:text-yellow-300">
                {errorMessage}
              </span>
            )}

            <button
              type="submit"
              disabled={isRegistering}
              className={`w-full px-4 py-2 font-medium rounded-lg transition duration-300
                focus:outline-none focus-visible:ring-2 focus-visible:ring-offset-2
                focus-visible:ring-indigo-600 contrast:focus-visible:ring-yellow-300
                focus-visible:ring-offset-white dark:focus-visible:ring-offset-gray-900
                contrast:focus-visible:ring-offset-black
                ${
                  isRegistering
                    ? 'bg-gray-300 text-gray-600 cursor-not-allowed dark:bg-gray-700 dark:text-gray-300'
                    : 'bg-indigo-600 text-white hover:bg-indigo-700 hover:shadow-xl contrast:bg-yellow-300 contrast:text-black contrast:hover:bg-yellow-200'
                }`}
            >
              {isRegistering ? 'Signing Up...' : 'Sign Up'}
            </button>

            <div className="text-sm text-center text-gray-600 dark:text-gray-300 contrast:text-white">
              Already have an account?{' '}
              <Link
                to={'/login'}
                className="font-bold hover:underline contrast:underline contrast:decoration-yellow-300"
              >
                Continue
              </Link>
            </div>
          </form>
        </div>
      </main>
    </div>
  )
}

export default Register
