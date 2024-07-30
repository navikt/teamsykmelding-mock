import { BodyShort, Label } from '@navikt/ds-react'
import { ReactElement, HTMLAttributes, PropsWithChildren } from 'react'
import {
    Combobox,
    ComboboxInput,
    ComboboxPopover,
    ComboboxOption,
    ComboboxInputProps,
    ComboboxPopoverProps,
    ComboboxProps,
    ComboboxOptionProps,
} from '@reach/combobox'
import cn from 'clsx'

import styles from './Combobox.module.css'

export function ComboboxWrapper({
    id,
    label,
    children,
}: PropsWithChildren<{ id?: string; label: string }>): ReactElement {
    return (
        <div className="navds-form-field navds-form-field--medium">
            <Label id={id}>{label}</Label>
            {children}
        </div>
    )
}

export function DsCombobox({
    children,
    className,
    ...props
}: PropsWithChildren<HTMLAttributes<HTMLDivElement> & ComboboxProps>): ReactElement {
    return (
        <Combobox className={cn(className, 'navds-select__container')} {...props}>
            {children}
        </Combobox>
    )
}

export function DsComboboxInput({
    children,
    className,
    ...props
}: PropsWithChildren<HTMLAttributes<HTMLInputElement> & ComboboxInputProps>): ReactElement {
    return (
        <ComboboxInput
            className={cn(
                className,
                'navds-search__input navds-search__input--secondary navds-text-field__input navds-body-short navds-body-medium',
            )}
            {...props}
        >
            {children}
        </ComboboxInput>
    )
}

export function DsComboboxPopover({
    children,
    className,
    ...props
}: PropsWithChildren<HTMLAttributes<HTMLInputElement> & ComboboxPopoverProps>): ReactElement {
    return (
        <ComboboxPopover className={cn(className, styles.suggestionPopover)} {...props}>
            {children}
        </ComboboxPopover>
    )
}

export function DsComboboxOption({
    children,
    className,
    ...props
}: PropsWithChildren<HTMLAttributes<HTMLLIElement>> & ComboboxOptionProps): ReactElement {
    return (
        <ComboboxOption className={cn(className, 'navds-body-short', styles.suggestion)} {...props}>
            {children}
        </ComboboxOption>
    )
}

export function DsComboboxNoResult({ text }: { text: string }): ReactElement {
    return <BodyShort className={cn(styles.suggestionNoResult)}>{text}</BodyShort>
}
