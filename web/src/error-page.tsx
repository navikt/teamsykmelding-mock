import { useRouteError } from "react-router-dom";
import { BodyShort, Heading } from "@navikt/ds-react";

import image from "./images/error-page-image.webp";

export default function ErrorPage() {
  const error = useRouteError();

  console.error(error);

  return (
    <div className="h-screen">
      <div className="flex justify-center items-center h-full">
        <div className="flex flex-col justify-center items-center">
          <img src={image} height={256} width={256} />
          <Heading size="large" level="2">
            Hjeeelp en frontend feil!
          </Heading>
          <BodyShort>Du drepte MacGyver :(</BodyShort>
        </div>
      </div>
    </div>
  );
}
