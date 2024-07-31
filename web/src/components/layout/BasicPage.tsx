import { BodyShort, Heading } from "@navikt/ds-react";
import { PropsWithChildren, ReactElement } from "react";

type Props = {
  title: string;
};

function BasicPage({
  children,
  title,
}: PropsWithChildren<Props>): ReactElement {
  return (
    <div>
      <Heading size="medium" level="2" className="flex gap-2 items-center">
        {title}
      </Heading>
      <BodyShort size="small">{title}</BodyShort>
      <div className="mt-8">{children}</div>
    </div>
  );
}

export default BasicPage;
