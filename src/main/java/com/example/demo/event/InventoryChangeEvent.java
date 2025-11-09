package com.example.demo.event;

import com.example.demo.model.InventoryModel;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class InventoryChangeEvent extends ApplicationEvent {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final InventoryModel inventory;
    private final String operationType; // "UPDATE", "CREATE", "DELETE"
    private final Integer previousQuantity;
    
    public InventoryChangeEvent(Object source, InventoryModel inventory, String operationType, Integer previousQuantity) {
        super(source);
        this.inventory = inventory;
        this.operationType = operationType;
        this.previousQuantity = previousQuantity;
    }
}

