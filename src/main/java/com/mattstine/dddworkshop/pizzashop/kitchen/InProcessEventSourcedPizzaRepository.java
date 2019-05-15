package com.mattstine.dddworkshop.pizzashop.kitchen;

import com.mattstine.dddworkshop.pizzashop.infrastructure.events.ports.EventLog;
import com.mattstine.dddworkshop.pizzashop.infrastructure.events.ports.Topic;
import com.mattstine.dddworkshop.pizzashop.infrastructure.repository.adapters.InProcessEventSourcedRepository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

final class InProcessEventSourcedPizzaRepository extends InProcessEventSourcedRepository<PizzaRef, Pizza, Pizza.PizzaState, PizzaEvent, PizzaAddedEvent> implements PizzaRepository {
    Map<KitchenOrderRef, Set<PizzaRef>> kitchenOrderRefToPizzaRefs;

    InProcessEventSourcedPizzaRepository(EventLog eventLog, Topic pizzas) {
        super(eventLog, PizzaRef.class, Pizza.class, Pizza.PizzaState.class, PizzaAddedEvent.class, pizzas);

        kitchenOrderRefToPizzaRefs = new HashMap<>();

        eventLog.subscribe(new Topic("pizzas"), e -> {
            if (e instanceof PizzaAddedEvent) {
                PizzaAddedEvent pae = (PizzaAddedEvent) e;
                Set<PizzaRef> pizzaRefs = kitchenOrderRefToPizzaRefs.computeIfAbsent(pae.getState().getKitchenOrderRef(), k -> new HashSet<>());
                pizzaRefs.add(pae.getRef());
            }
        });
    }

    @Override
    public Set<Pizza> findPizzasByKitchenOrderRef(KitchenOrderRef kitchenOrderRef) {
        return kitchenOrderRefToPizzaRefs.get(kitchenOrderRef).stream()
                .map(this::findByRef)
                .collect(Collectors.toSet());
    }
}
