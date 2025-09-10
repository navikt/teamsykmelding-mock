import { getEnv } from "../utils/env.ts";

/**
 * A lot of APIs simply return a message
 */
export type SimpleMessage = { message: string }

/**
 * Used by react query to fetch paths from the Ktor server
 */

const apiUrl = getEnv('VITE_INPUT_DOLLY')

export function post<Payload, Response>(path: `/${string}`) {
    return async (data: NoInfer<Payload>): Promise<NoInfer<Response> | void> => {
        const response = await fetch(`${apiUrl}${path}`, {
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
