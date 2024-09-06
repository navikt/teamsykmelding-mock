import { PropsWithChildren, ReactElement, Children, isValidElement } from 'react'
import { UseMutationResult } from '@tanstack/react-query'
import { Alert, Heading, Loader } from '@navikt/ds-react'

type Props = {
    title: string
    mutations?: UseMutationResult<any, any, any, any>[]
}

/**
 * Build any form page with a title, actions and result section.
 *
 * This component MUST exist in a form context for the actions to work.
 *
 * Example:
 *
 * <FormProvider {...form}>
 *   <form onSubmit={form.handleSubmit((values) => ...)}>
 *      <FormPage
 *        title="Example form"
 *        mutations={[mutationOne, mutationTwo]}
 *      >
 *      ... your actual form fields
 *      {mutationOne.data && (
 *        <FormPage.FormResult variant="success">
 *          <BodyShort spacing>Example text</BodyShort>
 *          <BodyShort>Server said {mutationOne.data.whatever}</BodyShort>
 *        </FormPage.FormResult>
 *      )}
 *      <FormPage.FormActions>
 *        <Button
 *          type="submit"
 *          loading={opprettSykmeldingMutation.isPending}
 *          disabled={sjekkRegelMutation.isPending}
 *        >
 *          Opprett example
 *        </Button>
 *      </FormPage.FormActions>
 *    </FormPage>
 *   </form>
 * </FormProvider>
 *
 */
function FormPage({ title, mutations, children }: PropsWithChildren<Props>): ReactElement {
    const anyMutationLoading = mutations?.some((mutation) => mutation.isPending)

    const actionSection = Children.toArray(children).find((child) => {
        return isValidElement(child) && child.type === FormPage.FormActions
    })

    const resultSection = Children.toArray(children).find((child) => {
        return isValidElement(child) && child.type === FormPage.FormResult
    })

    const otherChildren = Children.toArray(children).filter(
        (child) => isValidElement(child) && child.type !== FormPage.FormActions && child.type !== FormPage.FormResult,
    )

    return (
        <div title={title} className="relative">
            <Heading size="large" level="2" className="flex gap-2 items-center m-4 mb-0">
                {title}
            </Heading>

            <div className="p-4">
                {otherChildren}
                {anyMutationLoading && (
                    <div className="absolute top-0 left-0 h-full w-full bg-bg-subtle opacity-30 flex items-center justify-center">
                        <Loader size="3xlarge" />
                    </div>
                )}
            </div>
            <div className="sticky bottom-0 bg-white z-10 border-t border-t-border-subtle">
                {resultSection && <div>{resultSection}</div>}
                <div className="p-4 flex gap-4">{actionSection}</div>
            </div>
        </div>
    )
}

function FormPageActions({ children }: PropsWithChildren) {
    return <>{children}</>
}

function FormPageResult({ children, variant }: PropsWithChildren<{ variant: 'success' | 'info' | 'error' }>) {
    return (
        <Alert variant={variant} className="border-0 border-t border-b rounded-none">
            {children}
        </Alert>
    )
}

FormPage.FormActions = FormPageActions
FormPage.FormResult = FormPageResult

export default FormPage
