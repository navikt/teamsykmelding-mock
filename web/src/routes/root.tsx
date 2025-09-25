import { ReactElement } from 'react'
import { Outlet } from 'react-router-dom'
import { Page } from '@navikt/ds-react'

import Header from '../components/layout/Header.tsx'
import Sidebar from '../components/layout/Sidebar.tsx'
import SidebarDolly from '../components/layout/SidebarDolly.tsx'

import styles from './root.module.css'

export default function Root(): ReactElement {
    return (
        <Page contentBlockPadding="none">
            <Header />
            <div className={styles.content}>
                <Sidebar className={styles.sidebar} />
                <Page.Block width="2xl" as="main">
                    <Outlet />
                </Page.Block>
            </div>
        </Page>
    )
}

export function RootDolly(): ReactElement {
    return (
        <Page contentBlockPadding="none">
            <Header />
            <div className={styles.content}>
                <SidebarDolly className={styles.sidebar} />
                <Page.Block width="2xl" as="main">
                    <Outlet />
                </Page.Block>
            </div>
        </Page>
    )
}
