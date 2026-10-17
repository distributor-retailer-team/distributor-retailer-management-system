
package com.distributor.retailer.controller;

import com.distributor.retailer.entity.Retailer;
import com.distributor.retailer.service.RetailerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/retailers")
public class RetailerController {

    private final RetailerService retailerService;

    public RetailerController(RetailerService retailerService) {
        this.retailerService = retailerService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Retailer createRetailer(@Valid @RequestBody Retailer retailer) {
        return retailerService.createRetailer(retailer);
    }

    @GetMapping
    public List<Retailer> getAllRetailers() {
        return retailerService.getAllRetailers();
    }

    @GetMapping("/{id}")
    public Retailer getRetailerById(@PathVariable Long id) {
        return retailerService.getRetailerById(id);
    }

    @GetMapping("/search")
    public List<Retailer> searchRetailers(@RequestParam String name) {
        return retailerService.searchRetailersByName(name);
    }

    @PutMapping("/{id}")
    public Retailer updateRetailer(
            @PathVariable Long id,
            @Valid @RequestBody Retailer retailer) {
        return retailerService.updateRetailer(id, retailer);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRetailer(@PathVariable Long id) {
        retailerService.deleteRetailer(id);
    }
}
