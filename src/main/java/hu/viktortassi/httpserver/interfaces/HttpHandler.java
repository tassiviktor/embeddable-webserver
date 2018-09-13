package hu.viktortassi.httpserver.interfaces;

import hu.viktortassi.httpserver.HttpExchange;

public interface HttpHandler {
    
    public void handle(HttpExchange exchange);
    
}
