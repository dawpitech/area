// src/components/workflows/index.jsx

import React, { useState } from 'react'

const mockWorkflows = [
  { id: 1, name: 'My first workflow' },
  { id: 2, name: 'GitHub to Discord alert' },
]

const Workflows = () => {
  const [selectedId, setSelectedId] = useState(mockWorkflows[0]?.id || null)

  const selectedWorkflow = mockWorkflows.find(w => w.id === selectedId)

  return (
    <main className="w-full min-h-screen pt-16 px-4 bg-gray-50">
      <div className="max-w-6xl mx-auto flex border rounded-xl shadow bg-white overflow-hidden">
        <aside className="w-64 border-r bg-gray-50">
          <div className="px-4 py-3 border-b">
            <h2 className="text-sm font-semibold text-gray-700">
              Workflows
            </h2>
          </div>

          <ul className="py-2">
            {mockWorkflows.map(workflow => (
              <li key={workflow.id}>
                <button
                  type="button"
                  onClick={() => setSelectedId(workflow.id)}
                  className={
                    'w-full text-left px-4 py-2 text-sm transition ' +
                    (workflow.id === selectedId
                      ? 'bg-blue-600 text-white'
                      : 'hover:bg-gray-100 text-gray-700')
                  }
                >
                  {workflow.name}
                </button>
              </li>
            ))}
          </ul>
        </aside>

        <section className="flex-1 p-6">
          <h1 className="text-xl font-semibold mb-4 text-gray-800">
            {selectedWorkflow ? selectedWorkflow.name : 'No workflow selected'}
          </h1>

          <div className="w-full min-h-[300px] border border-dashed border-gray-300 rounded-lg flex items-center justify-center text-gray-400 text-sm">
            Main workflow content goes here
          </div>
        </section>
      </div>
    </main>
  )
}

export default Workflows
