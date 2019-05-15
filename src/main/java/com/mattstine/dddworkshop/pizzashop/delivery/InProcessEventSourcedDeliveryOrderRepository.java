package com.mattstine.dddworkshop.pizzashop.delivery;

import com.mattstine.dddworkshop.pizzashop.infrastructure.events.ports.EventLog;
import com.mattstine.dddworkshop.pizzashop.infrastructure.events.ports.Topic;
import com.mattstine.dddworkshop.pizzashop.infrastructure.repository.adapters.InProcessEventSourcedRepository;
import com.mattstine.dddworkshop.pizzashop.kitchen.KitchenOrderRef;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Matt Stine
 */
final class InProcessEventSourcedDeliveryOrderRepository extends InProcessEventSourcedRepository<DeliveryOrderRef, DeliveryOrder, DeliveryOrder.OrderState, DeliveryOrderEvent, DeliveryOrderAddedEvent> implements DeliveryOrderRepository {
	Map<KitchenOrderRef, DeliveryOrderRef> kitchenOrderRefToDeliveryOrderRef;

	InProcessEventSourcedDeliveryOrderRepository(EventLog eventLog, Topic topic) {
		super(eventLog,
				DeliveryOrderRef.class,
				DeliveryOrder.class,
				DeliveryOrder.OrderState.class,
				DeliveryOrderAddedEvent.class,
				topic);

		kitchenOrderRefToDeliveryOrderRef = new HashMap<>();

		eventLog.subscribe(new Topic("delivery_orders"), e -> {
			if (e instanceof DeliveryOrderAddedEvent) {
				DeliveryOrderAddedEvent doae = (DeliveryOrderAddedEvent) e;
				kitchenOrderRefToDeliveryOrderRef.put(doae.getState().getKitchenOrderRef(), doae.getRef());
			}
		});
	}

	@Override
	public DeliveryOrder findByKitchenOrderRef(KitchenOrderRef kitchenOrderRef) {
		DeliveryOrderRef deliveryOrderRef = kitchenOrderRefToDeliveryOrderRef.get(kitchenOrderRef);
		if (deliveryOrderRef != null) {
			return findByRef(deliveryOrderRef);
		}
		return null;
	}
}
