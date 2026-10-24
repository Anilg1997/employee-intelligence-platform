import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  // Keep SSR available at request time, but do not prerender data-dependent
  // routes during the build. Those routes load tenant-scoped API data only in
  // the browser/request context where the backend and identity are available.
  {
    path: '**',
    renderMode: RenderMode.Server
  }
];
