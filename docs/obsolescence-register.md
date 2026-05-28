# Habitv obsolescence register

Track obsolete providers, endpoints, and features deliberately. Canonical
rule: [`AGENTS.md`](../AGENTS.md) Section 18.16.

**Related inventory:** [`provider-inventory.md`](provider-inventory.md) —
current provider status table.

Do not delete legacy providers silently. Mark, replace, deprecate, or remove
in focused PRs.

## Register template

| Provider / feature | Old endpoint or behavior | Replacement | Status | Removal plan | Risk | Related PR |
|--------------------|--------------------------|-------------|--------|--------------|------|------------|
| *(example)* | *(dead URL)* | *(new service)* | obsolete | *(tracker/PR)* | low | — |

### Status values

Use the provider status matrix from [`provider-policy.md`](provider-policy.md):
working, degraded, protected, obsolete, removed, unknown.

## When to add an entry

- endpoint confirmed dead or replaced;
- provider removed from active list;
- external tool replaced (see [`external-tools-recommendations.md`](external-tools-recommendations.md));
- platform DRM/auth change makes prior approach obsolete.

## When to update

- provider restored or degraded status changes;
- replacement service validated;
- removal completed — move to **removed** with PR reference.

## Initial notes

Populate from [`provider-inventory.md`](provider-inventory.md) and tracker
items as providers are validated. This register is the **removal/obsolete
planning** view; inventory is the **current status** view.
