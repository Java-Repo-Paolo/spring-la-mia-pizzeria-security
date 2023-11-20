package com.experis.course.springlamiapizzeriacrud.controller;

import com.experis.course.springlamiapizzeriacrud.model.Ingredient;
import com.experis.course.springlamiapizzeriacrud.model.Pizza;
import com.experis.course.springlamiapizzeriacrud.repository.IngredientRepository;
import com.experis.course.springlamiapizzeriacrud.repository.PizzaRepository;
import com.experis.course.springlamiapizzeriacrud.security.DatabaseUserDetails;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/pizzas")
public class PizzaController {

    @Autowired
    private PizzaRepository pizzaRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @GetMapping
    public String index(@RequestParam Optional<String> search, Model model, Authentication authentication){
        List<Pizza> pizzaList;
        DatabaseUserDetails user = (DatabaseUserDetails) authentication.getPrincipal();
        /*System.out.println(user.getUsername());
        System.out.println(user.getAuthorities());*/
        if (search.isPresent()){
            pizzaList = pizzaRepository.findByNameContainingIgnoreCase(search.get());
        }else {

            pizzaList = pizzaRepository.findAll();
        }
        model.addAttribute("pizzaList", pizzaList);
        return "pizzas/list";
    }

    @GetMapping("/show/{id}")
    public String show(@PathVariable Integer id, Model model){
        Optional<Pizza> result = pizzaRepository.findById(id);
        if (result.isPresent()){
            model.addAttribute("pizza", result.get());
            return "pizzas/show";
        }else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "pizza with id " + id + " not found");
        }

    }

    @GetMapping("/create")
    public String create(Model model){
        model.addAttribute("pizza", new Pizza());

        List<Ingredient> ingredientList = ingredientRepository.findByOrderByName();
        model.addAttribute("ingredientList", ingredientList);
        return "pizzas/form";

    }

    @PostMapping("/create")
    public String store(@Valid @ModelAttribute("pizza") Pizza formPizza,
        BindingResult bindingResult, Model model){
        if(bindingResult.hasErrors()){

            List<Ingredient> ingredientList = ingredientRepository.findByOrderByName();
            model.addAttribute("ingredientList", ingredientList);

            return "pizzas/form";
        }
        formPizza.setCreatedAt(LocalDateTime.now());
        Pizza savedPizza = pizzaRepository.save(formPizza);
        return "redirect:/pizzas";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model){
        Optional<Pizza> result = pizzaRepository.findById(id);
        // Verifico se il risultato è presente
        if (result.isPresent()) {
            model.addAttribute("pizza", result.get());

            List<Ingredient> ingredientList = ingredientRepository.findByOrderByName();
            model.addAttribute("ingredientList", ingredientList);

            return "pizzas/form";
        } else {
            // se non ho trovato la pizza sollevo un'eccezione
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "pizza with id " + id + " not found");
        }
    }

    @PostMapping("/edit/{id}")
    public String doEdit (@PathVariable Integer id,
                          @Valid @ModelAttribute("pizza") Pizza formPizza,
                          BindingResult bindingResult, Model model) {
        if(bindingResult.hasErrors()){
            List<Ingredient> ingredientList = ingredientRepository.findByOrderByName();
            model.addAttribute("ingredientList", ingredientList);

            return "/pizzas/form";
        }else{
            Pizza pizzaToEdit = pizzaRepository.findById(id)
                    .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND));

            pizzaToEdit.setName(formPizza.getName());
            pizzaToEdit.setPrice(formPizza.getPrice());
            pizzaToEdit.setImage(formPizza.getImage());
            pizzaToEdit.setDescription(formPizza.getDescription());
            pizzaToEdit.setIngredients(formPizza.getIngredients());

            Pizza savedPizza = pizzaRepository.save(pizzaToEdit);
            return "redirect:/pizzas/show/" + savedPizza.getId();
        }
    }

    @PostMapping("delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes){
        Pizza pizzaToDelete = pizzaRepository.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND));
        pizzaRepository.deleteById(id);
        redirectAttributes.addFlashAttribute(
            "message",
            pizzaToDelete.getName()
                        + " deleted!"
        );
        return "redirect:/pizzas";
    }
}
