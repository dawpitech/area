import React from 'react'
import { useAuth } from '../../contexts/authContext'

const Home = () => {
  const { email } = useAuth()

  return (
    <div className="text-2xl font-bold pt-14">
      {email
        ? <>Hello {email}, you are now logged in.</>
        : <>Hello, you are now logged in.</>}
    </div>
  )
}

export default Home
