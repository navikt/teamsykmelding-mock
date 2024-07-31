import { useCallback, useState } from 'react'

type ActionOpts = {
    pathParam?: string
    fnr?: string
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    responseMapper?: (response: any) => string
}

export function useProxyDelete(path: string): [
    mutation: (actionOptions: ActionOpts) => void,
    status: {
        loading: boolean
        error: string | null
        result: string | null
    },
] {
    const [mutation, result] = useProxyAction<never>(path, 'DELETE')

    return [(actionOptions) => mutation(undefined, actionOptions), result]
}

export function useProxyAction<BodyType>(
    path: string,
    method: 'DELETE' | 'POST' = 'POST',
): [
    mutation: (data: BodyType | undefined, actionOptions?: ActionOpts) => void,
    status: {
        loading: boolean
        error: string | null
        result: string | null
        reset: () => void
    },
] {
    const [error, setError] = useState<string | null>(null)
    const [result, setResult] = useState<string | null>(null)
    const [loading, setLoading] = useState<boolean>(false)

    const action = useCallback(
        async (data: BodyType | undefined, { pathParam, fnr, responseMapper }: ActionOpts = {}) => {
            setLoading(true)
            setError(null)
            setResult(null)

            try {
                const extraHeaders = fnr ? { 'Sykmeldt-Fnr': fnr } : undefined
                const response = await fetch(`/api/proxy${path}${pathParam ?? ''}`, {
                    method,
                    headers: { 'Content-Type': 'application/json', ...extraHeaders },
                    body: data ? JSON.stringify(data) : undefined,
                })

                const mapper = responseMapper ?? ((it) => it.message)
                if (response.ok) {
                    setResult(mapper(await response.json()))
                } else {
                    if (response.headers.get('Content-Type')?.includes('application/json')) {
                        setError((await response.json()).message)
                    } else {
                        setError(await response.text())
                    }
                }
            } finally {
                setLoading(false)
            }
        },
        [method, path],
    )

    return [
        action,
        {
            loading,
            error,
            result,
            reset: useCallback(() => {
                setError(null)
                setResult(null)
            }, []),
        },
    ]
}
