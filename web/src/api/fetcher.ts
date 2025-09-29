/**
 * A lot of APIs simply return a message
 */
export type SimpleMessage = { message: string }

/**
 * Used by react query to fetch paths from the Ktor server
 */
export async function fetcher<Data>(path: `/${string}`): Promise<Data>  {
    const response = await fetch(`/api${path}`, {
        headers: {
            'Content-Type': 'application/json',
        },
    })

    if (!response.ok) {
        let errorMessage = `An error occurred while fetching the data. ${response.statusText} (${response.status})`
        try {
            const data = await response.json()
            if (data && data.message) {
                errorMessage += ` Message: ${data.message}`
            }
        } catch {
            throw new Error(errorMessage)
        }
        throw new Error(errorMessage)
    }

    try {
        return await response.json()
    } catch (error) {
        throw new Error(`Failed to parse response ${error}`)
    }
}

export function post<Payload>(path: `/${string}`): (data: NoInfer<Payload>) => Promise<void>
export function post<Payload, Response>(path: `/${string}`): (data: NoInfer<Payload>) => Promise<NoInfer<Response>>
export function post<Payload, Response>(path: `/${string}`) {
    return async (data: NoInfer<Payload>): Promise<NoInfer<Response> | void> => {
        const response = await fetch(`/api${path}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data),
        })

        if (!response.ok) {
            throw new Error('An error occurred while fetching the data')
        }

        if (response.headers.get('Content-Type')?.includes('application/json')) {
            return response.json()
        }

        return
    }
}
