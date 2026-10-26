# Agenda landing page

Static, bilingual (ES/EN) landing page for the Agenda app, served by Cloudflare
Workers at `agenda.ilecto.app`.

## Deployment

```bash
cd site
npx wrangler deploy
```

This mirrors the per-project Worker pattern used across `*.ilecto.app`: one
small static Worker per project (`agenda-site` here), each owning only its
own custom-domain route — see [Novenario's `site/`](https://github.com/chlewey/Novenario)
and the [`ilecto-site` hub](https://github.com/Interlecto/ilecto) for the
sibling setups this one follows.

Update this page's content as the app's roadmap phases actually ship —
right now it reflects the Phase 0 skeleton state in [`../ROADMAP.md`](../ROADMAP.md).
