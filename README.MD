# JI Accreds v2

## Intentions

This service should handle everything related to accreditations, namely allowing someone to enter the festival.

 - TOP: Handle manual accreditation for guests, boothkeepers, ...
 - TOP: Handle automatic accreditation for staff members
 - TOP: Handle manual accreditation for comitee members
 - TOP: Handle automatic accreditation for visitors
 - LOW: Handle automatic generation of the badge for staff and comitee members
 
All tied to an event from Event API.

## Relationships with other products

### Visitors scanning

Accreds should hold some kind of unique identifier for the accred (id or UUID).

A new ticket service (or shop) should hold a map between a barcode and an accred.

That service should request accred to (1) check the accred content and (2) set the accred as used.

## Main problem

How to define accred types that are both generic enough to be reused seemlessly across all services, but at the same
time precise flexible so that we can change them without changing the code.

### Solution-ish

Enum `AccredKind` with `Visitor`, `Staff` and `Guest`

Nope, actually just use the accred types we introduced earlier. Maybe group them by custom categories, to make it easier to find them?

Make the accred types bound to an edition and not to nothing.

Accred field "is_automatic" => can only be accepted via API.

Accred field "is_read_only" => can only be modified via API.

## Random

- Ideally, we have only one accred type for all "1 day passes", for example. The price paid is kept somewhere else, and
the verification is also done elsewhere. + It would be great to directly have an option to "upgrade" a ticket from the
scan view, therefore grouping the tickets for real.