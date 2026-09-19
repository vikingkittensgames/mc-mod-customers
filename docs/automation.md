---
title: Automation
---

## Automation

Using Customer Pickup Counters and Customer Payment Boxes combined with some hoppers you
can automate a shop that customers can buy from.

* Setup your spawner with a Customer Pickup Counter above it as the counter/table block.
* Create your show and add at least one of the same Customer Pickup Counter block.
* In your shop add a Customer Payment Box.  This is where customers will drop their payments after they pickup their items.
* Add a hopper directed into your Customer Pickup counter block(s).
* Feed the items your customers want into that hopper like adding a barrel above it and filling it up.

![automation1.png]({{ '/screenshots/automation1.png' | relative_url }})

[automation1.mp4]({{ '/screenshots/automation1.mp4' | relative_url }})

As your customers spawn and head to your counter, the Customer Pickup Counter will recognize
what customers it is serving, get what items the customers want, and extract those items
from the hopper placing those items on the counter ready to pick up.  When the customer gets
there it will see the items it wants, take them, and drop the payment in the Customer Payment
Box.

Then you can break out your redstone skills or even work in the Create mod to automate the
crafting of the items before feeding them into the hopper, and maybe even using a comparator on
the hopper to know when it's empty to signal crafting more.

### Automation topics

* [Customer Payment Boxes]({{ '/automation/payment-boxes.html' | relative_url }})

