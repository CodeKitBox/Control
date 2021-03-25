package com.kits.control.adapter

import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem

class DefaultAdapter (items: List<AbstractFlexibleItem<*>?>?) :
        FlexibleAdapter<AbstractFlexibleItem<*>?>(items)