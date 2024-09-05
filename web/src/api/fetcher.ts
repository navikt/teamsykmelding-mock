/**
 * A lot of APIs simply return a message
 */
export type SimpleMessage = { message: string }

/**
 * Used by react query to fetch paths from the Ktor server
 */
export async function fetcher<Data>(path: `/${string}`) {
    return async (): Promise<Data> => {
        const response = await fetch(`/api${path}`, {
            headers: {
                'Content-Type': 'application/json',
            },
        })

        if (!response.ok) {
            throw new Error('An error occurred while fetching the data')
        }

        // Note: Doesn't actually validate the response from the server
        return response.json()
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
