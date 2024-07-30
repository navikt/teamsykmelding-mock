import 'next-logger'
import '@navikt/next-logger'

import { NextApiRequest, NextApiResponse } from 'next'
import { proxyApiRouteRequest } from '@navikt/next-api-proxy'
import { createProxyMiddleware } from 'http-proxy-middleware';

import { getEnv } from '../../../utils/env'
import {logger} from "@navikt/next-logger";

const proxy = createProxyMiddleware({
    target: 'http://localhost:8080', // Backend server URL
    changeOrigin: true,
    pathRewrite: {
        '^/api': '/api', // Fjerner `/api` fra stien før den sendes til backend
    },
});


const handler = async (req: NextApiRequest, res: NextApiResponse): Promise<void> => {
    if (process.env.NODE_ENV === 'development') {
        return new Promise((resolve, reject) => {
            proxy(req, res, (result: any) => {
                if (result instanceof Error) {
                    return reject(result);
                }
                return resolve(result);
            });
        });
    } else if (process.env.NODE_ENV === 'test'){
        await new Promise((resolve) => setTimeout(resolve, 340))

        res.status(200).json({ message: 'Jobber lokalt, 200 ok læll' })
        return
    }

    const rewrittenPath = req.url!.replace(`/api/proxy`, '')
    await proxyApiRouteRequest({
        path: rewrittenPath,
        req,
        res,
        hostname: getEnv('MOCK_BACKEND_URL'),
        https: false,
    })
}

export const config = {
    api: {
        bodyParser: false,
        externalResolver: true,
    },
}

export default handler
