package provider.controller;

import com.github.javaparser.JavaParser;
import com.github.javaparser.Position;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.springframework.web.bind.annotation.*;
import provider.response.ContextVariable;
import provider.response.ParseResult;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@RestController
public class JavaParserController {
    public JavaParserController() {
        try {
            TypeSolver reflectionTypeSolver = new ReflectionTypeSolver();
            TypeSolver poiSolver = new JarTypeSolver(this.getClass().getResourceAsStream("/poi-4.0.0.jar"));
            TypeSolver combinedTypeSolver = new CombinedTypeSolver(reflectionTypeSolver, poiSolver);


            JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
            JavaParser.getStaticConfiguration().setSymbolResolver(symbolSolver);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Set<ContextVariable> parseContextVariable(CompilationUnit cu, int row, int column) {
        Set<ContextVariable> contextVariables = new HashSet<>();

        //System.out.println(row + " : " + column);
        cu.accept(new VoidVisitorAdapter<Object>() {
            @Override
            public void visit(BlockStmt n, Object arg) {
                if (n.getRange().get().contains(new Range(new Position(row, column), new Position(row, column)))) {
                    super.visit(n, arg);
                }
            }

            @Override
            public void visit(VariableDeclarator n, Object arg) {
                if (n.getRange().get().begin.line <= row) {
                    try {
                        ResolvedValueDeclaration rn = n.resolve();
                        ContextVariable cv = new ContextVariable(rn.getType().describe(), rn.getName(), 0, 0);
                        contextVariables.add(cv);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                super.visit(n, arg);
            }
        }, null);

        return contextVariables;
    }

    public Set<String> parseImports(CompilationUnit cu) {
        Set<String> imports = new HashSet<>();

        cu.accept(new VoidVisitorAdapter<Object>() {
            @Override
            public void visit(ImportDeclaration n, Object arg) {
                super.visit(n, arg);
                imports.add(n.getNameAsString());
            }
        }, null);

        return imports;
    }

    @RequestMapping("/parse")
    @CrossOrigin
    public ParseResult getContextVariables(@RequestParam(name = "row", defaultValue = "0") String row,
                                           @RequestParam(name = "column", defaultValue = "0") String column,
                                           @RequestBody String javaText) {
        //System.out.println(javaText);
        CompilationUnit cu = null;
        try {
            cu = JavaParser.parse(javaText);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ParseResult(parseContextVariable(cu, Integer.parseInt(row), Integer.parseInt(column)), parseImports(cu));
        //return new HashSet<>();
    }
}
