import { ApplicationConfig, InjectionToken, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter, withViewTransitions } from '@angular/router';
import { routes } from './app.routes';
import { provideClientHydration, withEventReplay } from '@angular/platform-browser';

// Connect-RPC Imports
import { createGrpcWebTransport } from '@connectrpc/connect-web';
import { createClient, Client, Interceptor } from '@connectrpc/connect';
// IMPORT DIRECTLY FROM _pb NOW
import { CeremonyService, UserService, TeamService, AnalyticsService } from '../proto-gen/holocron/v1/ceremony_pb';

// Interceptor to inject the JWT
const authInterceptor: Interceptor = (next) => async (req) => {
  if (typeof window !== 'undefined' && window.localStorage) {
    const token = localStorage.getItem('holocron_jwt');
    if (token) {
      req.header.set('Authorization', `Bearer ${token}`);
    }
  }
  return await next(req);
};

// Set up the gRPC-Web transport to point to Armeria
const transport = createGrpcWebTransport({
  baseUrl: 'http://localhost:8080',
  interceptors: [authInterceptor]
});

// Create the v2 client
const ceremonyClient = createClient(CeremonyService, transport);
const userClient = createClient(UserService, transport);
const teamClient = createClient(TeamService, transport);
const analyticsClient = createClient(AnalyticsService, transport);

// Create an Angular Injection Token for strict typing in your components
export const CEREMONY_CLIENT = new InjectionToken<Client<typeof CeremonyService>>('CeremonyClient');
export const USER_CLIENT = new InjectionToken<Client<typeof UserService>>('UserClient');
export const TEAM_CLIENT = new InjectionToken<Client<typeof TeamService>>('TeamClient');
export const ANALYTICS_CLIENT = new InjectionToken<Client<typeof AnalyticsService>>('AnalyticsClient');

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes, withViewTransitions()),
    provideClientHydration(withEventReplay()),

    // Provide the client globally using the token
    { provide: CEREMONY_CLIENT, useValue: ceremonyClient },
    { provide: USER_CLIENT, useValue: userClient },
    { provide: TEAM_CLIENT, useValue: teamClient },
    { provide: ANALYTICS_CLIENT, useValue: analyticsClient }
  ]
};

