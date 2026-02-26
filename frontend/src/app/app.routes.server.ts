import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  {
    path: ':teamId/ceremony/:id',
    renderMode: RenderMode.Server
  },
  {
    path: ':teamId/create/:id/results',
    renderMode: RenderMode.Server
  },
  {
    path: '**',
    renderMode: RenderMode.Prerender
  }
];
