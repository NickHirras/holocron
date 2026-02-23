import { ApplicationConfig, InjectionToken, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { provideClientHydration, withEventReplay } from '@angular/platform-browser';

// Connect-RPC Imports
import { createGrpcWebTransport } from '@connectrpc/connect-web';
import { createClient, Client } from '@connectrpc/connect';
// IMPORT DIRECTLY FROM _pb NOW
import { CeremonyService } from '../proto-gen/holocron/v1/ceremony_pb';

// Set up the gRPC-Web transport to point to Armeria
const transport = createGrpcWebTransport({
  baseUrl: 'http://localhost:8080',
});

// Create the v2 client
const ceremonyClient = createClient(CeremonyService, transport);

// Create an Angular Injection Token for strict typing in your components
export const CEREMONY_CLIENT = new InjectionToken<Client<typeof CeremonyService>>('CeremonyClient');

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideClientHydration(withEventReplay()),

    // Provide the client globally using the token
    { provide: CEREMONY_CLIENT, useValue: ceremonyClient }
  ]
};

