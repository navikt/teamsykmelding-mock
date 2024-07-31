import { ReactElement } from "react";
import { Outlet } from "react-router-dom";
import { Page } from "@navikt/ds-react";

import Header from "../components/layout/Header.tsx";
import Sidebar from "../components/layout/Sidebar.tsx";

import styles from "./root.module.css";

function Root(): ReactElement {
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
  );
}

export default Root;
