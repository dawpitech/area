import React, { useEffect, useState } from 'react'

const THEME_KEY = 'theme'

const applyTheme = (theme) => {
  const root = document.documentElement
  if (theme === 'dark') {
    root.classList.add('dark')
    localStorage.setItem(THEME_KEY, 'dark')
  } else {
    root.classList.remove('dark')
    localStorage.setItem(THEME_KEY, 'light')
  }
}

const Settings = () => {
  const [theme, setTheme] = useState('light')

  useEffect(() => {
    const savedTheme = localStorage.getItem(THEME_KEY) || 'light'
    setTheme(savedTheme)
    applyTheme(savedTheme)
  }, [])

  const toggleTheme = () => {
    const nextTheme = theme === 'light' ? 'dark' : 'light'
    setTheme(nextTheme)
    applyTheme(nextTheme)
  }

  return (
    <main className="w-full min-h-screen pt-12 pl-64 px-6 bg-gray-50 dark:bg-gray-900 transition-colors">
      <div className="max-w-3xl">
        <h1 className="text-2xl font-semibold text-gray-800 dark:text-gray-100 mb-6">
          Settings
        </h1>

        <div className="bg-white dark:bg-gray-800 border dark:border-gray-700 rounded-xl shadow p-6">
          <h2 className="text-lg font-medium text-gray-800 dark:text-gray-100 mb-4">
            Visual settings
          </h2>

          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-700 dark:text-gray-200">
                Light/Dark mode
              </p>
              <p className="text-xs text-gray-500 dark:text-gray-400">
                Toogle Light/Dark mode within the application
              </p>
            </div>

            <button
              type="button"
              onClick={toggleTheme}
              className={
                'relative inline-flex h-6 w-11 items-center rounded-full transition ' +
                (theme === 'dark'
                  ? 'bg-blue-600'
                  : 'bg-gray-300')
              }
            >
              <span
                className={
                  'inline-block h-4 w-4 transform rounded-full bg-white transition ' +
                  (theme === 'dark'
                    ? 'translate-x-6'
                    : 'translate-x-1')
                }
              />
            </button>
          </div>
        </div>
      </div>
    </main>
  )
}

export default Settings
