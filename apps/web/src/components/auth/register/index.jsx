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
      navigate('/home')
    } catch (err) {
      setErrorMessage(err.message || 'Erreur lors de la création du compte')
      setIsRegistering(false)
    }
  }

  return (
    <>
      {userLoggedIn && (<Navigate to={'/home'} replace={true} />)}

      <main className="w-full h-screen flex self-center place-content-center place-items-center">
        <div className="w-96 text-gray-600 space-y-5 p-4 shadow-xl border rounded-xl">
          <div className="text-center mb-6">
            <div className="mt-2">
              <h3 className="text-gray-800 text-xl font-semibold sm:text-2xl">Create a New Account</h3>
            </div>
          </div>
          <form
            onSubmit={onSubmit}
            className="space-y-4"
          >
            <div>
              <label className="text-sm text-gray-600 font-bold">
                Email
              </label>
              <input
                type="email"
                autoComplete='email'
                required
                value={email}
                onChange={(e) => { setEmail(e.target.value) }}
                className="w-full mt-2 px-3 py-2 text-gray-500 bg-transparent outline-none border focus:border-indigo-600 shadow-sm rounded-lg transition duration-300"
              />
            </div>

            <div>
              <label className="text-sm text-gray-600 font-bold">
                Password
              </label>
              <div className="relative mt-2">
                <input
                  disabled={isRegistering}
                  type={showPassword ? 'text' : 'password'}
                  autoComplete='new-password'
                  required
                  value={password}
                  onChange={(e) => { setPassword(e.target.value) }}
                  className="w-full px-3 py-2 pr-10 text-gray-500 bg-transparent outline-none border focus:border-indigo-600 shadow-sm rounded-lg transition duration-300"
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
            </div>

            <div>
              <label className="text-sm text-gray-600 font-bold">
                Confirm Password
              </label>
              <div className="relative mt-2">
                <input
                  disabled={isRegistering}
                  type={showConfirmPassword ? 'text' : 'password'}
                  autoComplete='off'
                  required
                  value={confirmPassword}
                  onChange={(e) => { setconfirmPassword(e.target.value) }}
                  className="w-full px-3 py-2 pr-10 text-gray-500 bg-transparent outline-none border focus:border-indigo-600 shadow-sm rounded-lg transition duration-300"
                />
                <button
                  type="button"
                  onClick={() => setShowConfirmPassword(prev => !prev)}
                  className="absolute inset-y-0 right-2 my-auto flex items-center justify-center text-gray-500 hover:text-indigo-600 transition"
                  aria-label={showConfirmPassword ? 'Masquer la confirmation de mot de passe' : 'Afficher la confirmation de mot de passe'}
                >
                  {showConfirmPassword ? <FiEyeOff size={18} /> : <FiEye size={18} />}
                </button>
              </div>
            </div>

            {errorMessage && (
              <span className='text-red-600 font-bold'>{errorMessage}</span>
            )}

            <button
              type="submit"
              disabled={isRegistering}
              className={`w-full px-4 py-2 text-white font-medium rounded-lg ${isRegistering ? 'bg-gray-300 cursor-not-allowed' : 'bg-indigo-600 hover:bg-indigo-700 hover:shadow-xl transition duration-300'}`}
            >
              {isRegistering ? 'Signing Up...' : 'Sign Up'}
            </button>
            <div className="text-sm text-center">
              Already have an account?{' '}
              <Link to={'/login'} className="text-center text-sm hover:underline font-bold">Continue</Link>
            </div>
          </form>
        </div>
      </main>
    </>
  )
}

export default Register
