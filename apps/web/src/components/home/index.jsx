import React, { useState, useEffect } from 'react'
import { useAuth } from '../../contexts/authContext'
import {
  apiGithubInit,
  apiGithubCheck,
  apiGoogleInit,
  apiGoogleCheck,
  apiNotionInit,
  apiNotionCheck,
} from '../../api/auth'
import { FaGithub } from 'react-icons/fa'
import { FcGoogle } from 'react-icons/fc'
import { SiNotion } from 'react-icons/si'

const Home = () => {
  const { email, token } = useAuth()

  const [githubConnected, setGithubConnected] = useState(false)
  const [checkingGithub, setCheckingGithub] = useState(false)

  const [googleConnected, setGoogleConnected] = useState(false)
  const [checkingGoogle, setCheckingGoogle] = useState(false)

  const [notionConnected, setNotionConnected] = useState(false)
  const [checkingNotion, setCheckingNotion] = useState(false)

  const checkProviders = async () => {
    if (!token) {
      setGithubConnected(false)
      setGoogleConnected(false)
      setNotionConnected(false)
      return
    }

    try {
      setCheckingGithub(true)
      setCheckingGoogle(true)
      setCheckingNotion(true)

      const [gh, gg, nn] = await Promise.allSettled([
        apiGithubCheck(),
        apiGoogleCheck(),
        apiNotionCheck(),
      ])

      setGithubConnected(gh.status === 'fulfilled' ? !!gh.value?.is_connected : false)
      setGoogleConnected(gg.status === 'fulfilled' ? !!gg.value?.is_connected : false)
      setNotionConnected(nn.status === 'fulfilled' ? !!nn.value?.is_connected : false)
    } catch (err) {
      setGithubConnected(false)
      setGoogleConnected(false)
      setNotionConnected(false)
    } finally {
      setCheckingGithub(false)
      setCheckingGoogle(false)
      setCheckingNotion(false)
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
    if (!token) {
      alert(`You must be logged in to connect ${provider}.`)
      return
    }

    try {
      let data
      if (provider === 'github' && !githubConnected) data = await apiGithubInit()
      if (provider === 'google' && !googleConnected) data = await apiGoogleInit()
      if (provider === 'notion' && !notionConnected) data = await apiNotionInit()

      if (data?.redirect_to) {
        window.open(data.redirect_to, '_blank', 'noopener,noreferrer')
      } else {
        alert('Error: missing redirect URL')
      }
    } catch (err) {
      alert(`Error during ${provider} login (see console).`)
    }
  }

  const btnBase =
    'flex items-center justify-center gap-2 px-4 py-3 rounded-lg border shadow-sm transition duration-300 ease-out ' +
    'focus:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 ' +
    'focus-visible:ring-blue-500 focus-visible:ring-offset-white ' +
    'dark:focus-visible:ring-offset-gray-900 ' +
    'contrast:focus-visible:ring-yellow-300 contrast:focus-visible:ring-offset-black'

  const btnHover =
    'hover:-translate-y-0.5 hover:shadow-xl hover:ring-2'

  const btnLight =
    'bg-white text-gray-900 border-gray-200 hover:ring-gray-300/70'

  const btnDark =
    'dark:bg-gray-900 dark:text-gray-100 dark:border-gray-700 dark:hover:ring-gray-600/70'

  return (
    <main className="w-full min-h-screen pt-12 pl-64 px-4 bg-gray-50 dark:bg-gray-900 contrast:bg-black transition-colors">
      <div className="max-w-5xl mx-auto">
        <h1 className="text-2xl md:text-3xl font-bold mb-6 text-gray-800 dark:text-gray-100 contrast:text-white text-center">
          {email ? (
            <>
              Hello{' '}
              <span className="text-indigo-600 dark:text-indigo-400 contrast:text-yellow-300">
                {email}
              </span>
              , you are now logged in.
            </>
          ) : (
            <>Hello, you are now logged in.</>
          )}
        </h1>

        <div className="bg-white dark:bg-gray-950 contrast:bg-black rounded-xl shadow-lg border border-gray-200 dark:border-gray-800 contrast:border-white px-6 py-6 mb-6">
          <h2 className="text-lg font-semibold text-gray-800 dark:text-gray-100 contrast:text-white mb-4">
            Account information
          </h2>

          <label className="text-sm font-medium text-gray-600 dark:text-gray-200 contrast:text-white">
            Email
          </label>
          <input
            type="email"
            value={email || ''}
            readOnly
            className="mt-1 w-full px-3 py-2 border rounded-lg outline-none
                       bg-gray-50 text-gray-700 border-gray-300
                       dark:bg-gray-900 dark:text-gray-100 dark:border-gray-700
                       contrast:bg-black contrast:text-white contrast:border-white"
          />
        </div>

        <div className="bg-white dark:bg-gray-950 contrast:bg-black rounded-xl shadow-lg border border-gray-200 dark:border-gray-800 contrast:border-white px-6 py-6">
          <h2 className="text-lg font-semibold mb-2 text-gray-800 dark:text-gray-100 contrast:text-white text-center">
            Connect your accounts
          </h2>
          <p className="text-sm text-gray-500 dark:text-gray-400 contrast:text-white/80 mb-6 text-center">
            Link your favorite platforms to unlock more features.
          </p>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <button
              onClick={() => handleConnect('github')}
              disabled={githubConnected || checkingGithub}
              className={`${btnBase} ${
                githubConnected
                  ? 'bg-green-600 text-white border-green-600 cursor-default contrast:bg-green-400 contrast:text-black'
                  : `${btnLight} ${btnDark} ${btnHover} contrast:bg-black contrast:text-white contrast:border-white contrast:hover:bg-white/10`
              } ${checkingGithub ? 'opacity-70 cursor-wait' : ''}`}
            >
              <FaGithub size={18} />
              <span className="font-medium text-sm">
                {checkingGithub ? 'Checking...' : githubConnected ? 'Connected to GitHub' : 'Connect GitHub'}
              </span>
            </button>

            <button
              onClick={() => handleConnect('google')}
              disabled={googleConnected || checkingGoogle}
              className={`${btnBase} ${
                googleConnected
                  ? 'bg-green-600 text-white border-green-600 cursor-default contrast:bg-green-400 contrast:text-black'
                  : `${btnLight} ${btnDark} ${btnHover} contrast:bg-black contrast:text-white contrast:border-white contrast:hover:bg-white/10`
              } ${checkingGoogle ? 'opacity-70 cursor-wait' : ''}`}
            >
              <FcGoogle size={18} />
              <span className="font-medium text-sm">
                {checkingGoogle ? 'Checking...' : googleConnected ? 'Connected to Google' : 'Connect Google'}
              </span>
            </button>

            <button
              onClick={() => handleConnect('notion')}
              disabled={notionConnected || checkingNotion}
              className={`${btnBase} ${
                notionConnected
                  ? 'bg-green-600 text-white border-green-600 cursor-default contrast:bg-green-400 contrast:text-black'
                  : `${btnLight} ${btnDark} ${btnHover} contrast:bg-black contrast:text-white contrast:border-white contrast:hover:bg-white/10`
              } ${checkingNotion ? 'opacity-70 cursor-wait' : ''}`}
            >
              <SiNotion size={18} />
              <span className="font-medium text-sm">
                {checkingNotion ? 'Checking...' : notionConnected ? 'Connected to Notion' : 'Connect Notion'}
              </span>
            </button>
          </div>
        </div>
      </div>
    </main>
  )
}

export default Home
