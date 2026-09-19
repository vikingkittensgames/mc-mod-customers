---
title: Gameplay and Shifts
---

## Gameplay and Shifts

If your Customer Spawner Block is ser to a mode other than Continuous and Manual,
you and other players will be working within shifts that have a start and end.  You
will get a progress bar that shows the shift, a progress bar that ticks down to the
end, and a heads up view of what all active customers want for that shift.

![orders-in-progress-bar.png]({{ '/screenshots/orders-in-progress-bar.png' | relative_url }})

At the end of a shift where at least one player or Automated system crafted or served an
item, you and the other players will get a scoreboard showing how well you did. Automated
activity appears with redstone as its profile image. Along with the number of stars earned
for that shift, the scoreboard shows a green checkmark when you earned enough stars to pass
the current level:

![scoreboard.png]({{ '/screenshots/scoreboard.png' | relative_url }})

### Levels and Leaderboards

Customer Spawner Blocks support a progression of levels, with each level configured in the
Customer Spawner UI. Each level has a required number of stars, and the Customer Leaderboard
stores players' previous shift scores to determine which level a spawner should use.

#### Configuring Levels

You can configure up to eight levels in the Customer Spawner UI. Each level has its own
inventory of items customers want to buy, maximum number of customers, enabled appearances,
and number of stars required to pass the level.

![level1.png]({{ '/screenshots/level1.png' | relative_url }})

![level2.png]({{ '/screenshots/level2.png' | relative_url }})

![level3.png]({{ '/screenshots/level3.png' | relative_url }})

Use earlier levels for simpler shifts with fewer item options, easier items to craft, and
fewer customers. Later levels can increase the variety and complexity of requested items
and raise the maximum number of customers. When a shift starts, the Customer Spawner reviews
all players in the area against the closest Customer Leaderboard and their saved scores. It
finds the lowest level those players should be on and uses that level for the shift.

#### Leaderboards

The Customer Leaderboard stores each Customer Spawner shift score by spawner, mode, level,
and player. Place a Customer Leaderboard in your build so players can see how they and others
are doing. It shows the stars each player earned for a level and a green checkmark when that
player has passed the level.

![leaderboard-block.png]({{ '/screenshots/leaderboard-block.png' | relative_url }})

![leaderboard.png]({{ '/screenshots/leaderboard.png' | relative_url }})

Craft a Customer Leaderboard with stripped logs around an iron ingot, paper, emerald, and
ink sac.

![leaderboard-crafting.png]({{ '/screenshots/leaderboard-crafting.png' | relative_url }})

