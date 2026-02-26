import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  {
    path: 'team/:teamId',
    renderMode: RenderMode.Server
  },
  {
    path: 'team/:teamId/dashboard',
    renderMode: RenderMode.Server
  },
  {
    path: 'team/:teamId/create',
    renderMode: RenderMode.Server
  },
  {
    path: 'team/:teamId/ceremony/:id',
    renderMode: RenderMode.Server
  },
  {
    path: 'team/:teamId/ceremony/:id/results',
    renderMode: RenderMode.Server
  },
  {
    path: '**',
    renderMode: RenderMode.Prerender
  }
];
