import { PropsWithChildren, ReactElement } from 'react'
import { Alert } from '@navikt/ds-react'

type ActionFeedbackProps = {
    error: Error | string | null
    result: string | null
}

function ActionFeedback({ result, error, children }: PropsWithChildren<ActionFeedbackProps>): ReactElement | null {
    const feedback = error ? (
        <Alert variant="error">{error instanceof Error ? error.message : error}</Alert>
    ) : result ? (
        <Alert variant="success">{result}</Alert>
    ) : null

    return (
        <div className="mt-4 relative">
            <div className="flex flex-row gap-4">{children}</div>
            {feedback ? <div className="h-24 mt-4">{feedback}</div> : <div className="mt-4 h-24" />}
        </div>
    )
}

export default ActionFeedback
