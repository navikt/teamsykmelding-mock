import {ReactElement, useEffect} from 'react'
import {Outlet, useLocation} from 'react-router-dom'
import { Page } from '@navikt/ds-react'

import Header from '../components/layout/Header.tsx'
import Sidebar from '../components/layout/Sidebar.tsx'

import styles from './root.module.css'

function Root(): ReactElement {
    const location = useLocation();

    useEffect(() => {
        if (window.location.hostname === 'teamsykmelding-mock.intern.dev.nav.no') {
            window.location.href = `https://teamsykmelding-mock.ansatt.dev.nav.no${location.pathname}${location.search}`;
        }
    }, [location]);
    return (
        <Page contentBlockPadding="none">
            <Header />
            <div className={styles.content}>
                <Sidebar className={styles.sidebar} />
                <Page.Block gutters width="2xl" as="main" className={styles.pageBlock}>
                    <Outlet />
                </Page.Block>
            </div>
        </Page>
    )
}

export default Root
