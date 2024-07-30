import '../styles/globals.css'
import '@reach/combobox/styles.css'

import { PropsWithChildren, ReactElement } from 'react'

import Sidebar from '../components/Sidebar'
import Header from '../components/Header'

export default function RootLayout({ children }: PropsWithChildren): ReactElement {
    return (
        <html lang="no">
            <body className="h-screen flex flex-col">
                <Header />
                <div className="flex grow">
                    <div className="max-w-[360px] shrink-0 border-r border-r-border-subtle">
                        <Sidebar />
                    </div>
                    <div className="p-4 grow relative">
                        <div className="max-w-4xl">{children}</div>
                    </div>
                </div>
            </body>
        </html>
    )
}
