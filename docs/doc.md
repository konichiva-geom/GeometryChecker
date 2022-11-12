# Syntax

Point (regex): [A-Z][0-9]*

Line: PointPoint

Angle: PointPointPoint

# Basic operations

new A - create a new point distinct from all the current points. This operator is needed to prevent typos. If there was
no 'new', then just writing a new point will create it silently, which is not good.

AB ∩ CD - intersect line relation. Returns Point (adds "in" relations to that Point)

HA ⊥ BC - perpendicular line relation (special case of intersect relation). Returns point (adds)

A in CD - "in" operator adds in relation (point in line, segment in line etc.)

A == B - same relation. Applicable for points, lines, segments, angle values. Also there is a != (
not same) relation.

CD != AB - check that lines are not the same

AB || CD - parallel line relation (lines are not the same).

check(...) - checks that relation is known