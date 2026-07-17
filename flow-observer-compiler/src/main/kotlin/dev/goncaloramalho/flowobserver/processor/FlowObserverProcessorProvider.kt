package dev.goncaloramalho.flowobserver.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import dev.goncaloramalho.flowobserver.processor.codegen.FlowObserverGenerator
import dev.goncaloramalho.flowobserver.processor.validation.FlowObserverValidator

class FlowObserverProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        FlowObserverProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger,
        )
}

class FlowObserverProcessor(
    codeGenerator: CodeGenerator,
    logger: KSPLogger,
) : SymbolProcessor {

    private val validator = FlowObserverValidator(logger)
    private val generator = FlowObserverGenerator(codeGenerator, logger)
    private var generated = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val plan = validator.validate(resolver)
        if (generated || plan.viewModels.isEmpty()) return emptyList()

        val originating = plan.originatingFiles.toTypedArray()
        generator.generate(plan, originating)
        generated = true
        return emptyList()
    }
}
