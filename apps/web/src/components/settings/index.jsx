import React, { useEffect, useState } from 'react'

const THEME_KEY = 'theme'
const CONTRAST_KEY = 'high_contrast'

const applyAppearance = ({ theme, highContrast }) => {
  const root = document.documentElement

  if (theme === 'dark') {
    root.classList.add('dark')
    localStorage.setItem(THEME_KEY, 'dark')
  } else {
    root.classList.remove('dark')
    localStorage.setItem(THEME_KEY, 'light')
  }

  if (highContrast) {
    root.classList.add('contrast')
    localStorage.setItem(CONTRAST_KEY, 'true')
  } else {
    root.classList.remove('contrast')
    localStorage.setItem(CONTRAST_KEY, 'false')
  }
}

const Settings = () => {
  const [theme, setTheme] = useState('light')
  const [highContrast, setHighContrast] = useState(false)

  useEffect(() => {
    const savedTheme = localStorage.getItem(THEME_KEY) || 'light'
    const savedContrast = localStorage.getItem(CONTRAST_KEY) === 'true'

    setTheme(savedTheme)
    setHighContrast(savedContrast)

    applyAppearance({ theme: savedTheme, highContrast: savedContrast })
  }, [])

  const toggleTheme = () => {
    const nextTheme = theme === 'light' ? 'dark' : 'light'
    setTheme(nextTheme)
    applyAppearance({ theme: nextTheme, highContrast })
  }

  const toggleHighContrast = () => {
    const next = !highContrast
    setHighContrast(next)
    applyAppearance({ theme, highContrast: next })
  }

  return (
    <main
      className="
        w-full min-h-screen pt-12 pl-64 px-6
        bg-gray-50 dark:bg-gray-900
        contrast:bg-black
        transition-colors
      "
    >
      <div className="max-w-3xl">
        <h1 className="text-2xl font-semibold text-gray-800 dark:text-gray-100 contrast:text-white mb-6">
          Settings
        </h1>

        <div
          className="
            bg-white dark:bg-gray-800 contrast:bg-black
            border border-gray-200 dark:border-gray-700 contrast:border-white
            rounded-xl shadow p-6
          "
        >
          <h2 className="text-lg font-medium text-gray-800 dark:text-gray-100 contrast:text-white mb-4">
            Visual settings
          </h2>

          <div className="flex items-center justify-between py-3">
            <div>
              <p className="text-sm font-medium text-gray-700 dark:text-gray-200 contrast:text-white">
                Light/Dark mode
              </p>
              <p className="text-xs text-gray-500 dark:text-gray-400 contrast:text-white/80">
                Toggle Light/Dark mode within the application
              </p>
            </div>

            <button
              type="button"
              role="switch"
              aria-checked={theme === 'dark'}
              aria-label="Toggle dark mode"
              onClick={toggleTheme}
              className={[
                'relative inline-flex h-6 w-11 items-center rounded-full transition',
                'focus:outline-none focus-visible:ring-2 focus-visible:ring-offset-2',
                'focus-visible:ring-blue-500 focus-visible:ring-offset-white dark:focus-visible:ring-offset-gray-900',
                'contrast:focus-visible:ring-yellow-300 contrast:focus-visible:ring-offset-black',
                theme === 'dark' ? 'bg-blue-600' : 'bg-gray-300',
                'contrast:bg-white',
              ].join(' ')}
            >
              <span
                className={[
                  'inline-block h-4 w-4 transform rounded-full transition',
                  'bg-white',
                  'contrast:bg-black',
                  theme === 'dark' ? 'translate-x-6' : 'translate-x-1',
                ].join(' ')}
              />
            </button>
          </div>

          <div className="flex items-center justify-between py-3 border-t border-gray-100 dark:border-gray-700 contrast:border-white/40">
            <div>
              <p className="text-sm font-medium text-gray-700 dark:text-gray-200 contrast:text-white">
                High contrast
              </p>
              <p className="text-xs text-gray-500 dark:text-gray-400 contrast:text-white/80">
                Improves readability with stronger colors and focus indicators
              </p>
            </div>

            <button
              type="button"
              role="switch"
              aria-checked={highContrast}
              aria-label="Toggle high contrast"
              onClick={toggleHighContrast}
              className={[
                'relative inline-flex h-6 w-11 items-center rounded-full transition',
                'focus:outline-none focus-visible:ring-2 focus-visible:ring-offset-2',
                'focus-visible:ring-blue-500 focus-visible:ring-offset-white dark:focus-visible:ring-offset-gray-900',
                'contrast:focus-visible:ring-yellow-300 contrast:focus-visible:ring-offset-black',
                highContrast ? 'bg-yellow-300' : 'bg-gray-300 dark:bg-gray-600',
                'contrast:bg-white',
              ].join(' ')}
            >
              <span
                className={[
                  'inline-block h-4 w-4 transform rounded-full transition',
                  highContrast ? 'translate-x-6' : 'translate-x-1',
                  highContrast ? 'bg-black' : 'bg-white',
                  'contrast:bg-black',
                ].join(' ')}
              />
            </button>
          </div>
        </div>
      </div>
    </main>
  )
}

export default Settings
