package com.numbers.projectaura.registries;

import com.numbers.projectaura.ProjectAura;
import com.numbers.projectaura.auras.IElementalAura;
import com.numbers.projectaura.reactions.ElementalReactions.FireOnWater;
import com.numbers.projectaura.reactions.ElementalReactions.WaterOnFire;
import com.numbers.projectaura.reactions.IElementalReaction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ReactionRegistry {
        private String MOD_ID;

        /**
         * The reaction registry is structured as such: a null parent node containing a node for each elemental type that has a reaction. Layer one is the base element, which contains the reactions for when an element is applied to it.
         * Thus, if you wanted to add a reaction where when Element X is applied to Element Y, you would file it as X_on_Y under Y.
         * Yes, I considered using a forge registry, but I really wanted the tree structure and making TreeForgeRegistry is beyond this scope... I'll eat my words later if I have to
          */
        private static HashMap<IElementalAura, HashMap<IElementalAura, Tuple<ResourceLocation, IElementalReaction<?, ?>>>> regisTree = new HashMap<>();// I'm sorry

        // Implement fancy tree registry behavior ooo
        public <A extends IElementalAura, B extends IElementalAura> Tuple<ResourceLocation, IElementalReaction<?, ?>> register(ResourceLocation name, IElementalReaction<A, B> reaction) {

                IElementalAura base = reaction.getBase().get();

                if (!regisTree.containsKey(base)) {
                        regisTree.put(base, new HashMap<>());
                }

                HashMap<IElementalAura, Tuple<ResourceLocation, IElementalReaction<?, ?>>> registered = regisTree.get(base);

                IElementalAura applied = reaction.getApplied().get();

                if (registered.containsKey(applied)) {
                        throw new IllegalArgumentException("Registry already contains an entry: " + name.toString());
                }

                registered.put(applied, new Tuple<>(name, reaction));

                return new Tuple<>(name, reaction);
        }

        public <A extends IElementalAura, B extends IElementalAura> Tuple<ResourceLocation, IElementalReaction<?, ?>> register(String name, IElementalReaction<A, B> reaction) {
                return register(new ResourceLocation(this.MOD_ID, name), reaction);
        }

        @Nullable
        public static IElementalReaction<?, ?> getReaction(IElementalAura applied, IElementalAura base) {

                if (!regisTree.containsKey(base)) {
                        return null;
                }

                HashMap<IElementalAura, Tuple<ResourceLocation, IElementalReaction<?, ?>>> reactions = regisTree.get(base);

                if (!reactions.containsKey(applied)) {
                        return null;
                }

                return reactions.get(applied).getB();
        }

        public ReactionRegistry(String modid) {
                this.MOD_ID = modid;
        }

        public static final ReactionRegistry REGISTRY = new ReactionRegistry(ProjectAura.MOD_ID);

        public static final Tuple<ResourceLocation, IElementalReaction<?, ?>> FIRE_ON_WATER = REGISTRY.register("fire_on_water", new FireOnWater());

        public static final Tuple<ResourceLocation, IElementalReaction<?, ?>> WATER_ON_FIRE = REGISTRY.register("water_on_fire", new WaterOnFire());

        public static void register() { }

        /**
         * Yet another tree implementation https://github.com/gt4dev/yet-another-tree-structure
         * @param <T>
         */
        private static class TreeNode<T> implements Iterable<TreeNode<T>> {

                public T data;
                public TreeNode<T> parent;
                public List<TreeNode<T>> children;

                public boolean isRoot() {
                        return parent == null;
                }

                public boolean isLeaf() {
                        return children.size() == 0;
                }

                private List<TreeNode<T>> elementsIndex;

                public TreeNode(T data) {
                        this.data = data;
                        this.children = new LinkedList<TreeNode<T>>();
                        this.elementsIndex = new LinkedList<TreeNode<T>>();
                        this.elementsIndex.add(this);
                }

                public TreeNode<T> addChild(T child) {
                        TreeNode<T> childNode = new TreeNode<T>(child);
                        childNode.parent = this;
                        this.children.add(childNode);
                        this.registerChildForSearch(childNode);
                        return childNode;
                }

                public int getLevel() {
                        if (this.isRoot())
                                return 0;
                        else
                                return parent.getLevel() + 1;
                }

                private void registerChildForSearch(TreeNode<T> node) {
                        elementsIndex.add(node);
                        if (parent != null)
                                parent.registerChildForSearch(node);
                }

                public TreeNode<T> findTreeNode(Comparable<T> cmp) {
                        for (TreeNode<T> element : this.elementsIndex) {
                                T elData = element.data;
                                if (cmp.compareTo(elData) == 0)
                                        return element;
                        }

                        return null;
                }

                @Override
                public String toString() {
                        return data != null ? data.toString() : "[data null]";
                }

                @Override
                public Iterator<TreeNode<T>> iterator() {
                        TreeNodeIter<T> iter = new TreeNodeIter<T>(this);
                        return iter;
                }


        }

        private static class TreeNodeIter<T> implements Iterator<TreeNode<T>> {

                enum ProcessStages {
                        ProcessParent, ProcessChildCurNode, ProcessChildSubNode
                }

                private TreeNode<T> treeNode;

                public TreeNodeIter(TreeNode<T> treeNode) {
                        this.treeNode = treeNode;
                        this.doNext = ProcessStages.ProcessParent;
                        this.childrenCurNodeIter = treeNode.children.iterator();
                }

                private ProcessStages doNext;
                private TreeNode<T> next;
                private Iterator<TreeNode<T>> childrenCurNodeIter;
                private Iterator<TreeNode<T>> childrenSubNodeIter;

                @Override
                public boolean hasNext() {

                        if (this.doNext == ProcessStages.ProcessParent) {
                                this.next = this.treeNode;
                                this.doNext = ProcessStages.ProcessChildCurNode;
                                return true;
                        }

                        if (this.doNext == ProcessStages.ProcessChildCurNode) {
                                if (childrenCurNodeIter.hasNext()) {
                                        TreeNode<T> childDirect = childrenCurNodeIter.next();
                                        childrenSubNodeIter = childDirect.iterator();
                                        this.doNext = ProcessStages.ProcessChildSubNode;
                                        return hasNext();
                                } else {
                                        this.doNext = null;
                                        return false;
                                }
                        }

                        if (this.doNext == ProcessStages.ProcessChildSubNode) {
                                if (childrenSubNodeIter.hasNext()) {
                                        this.next = childrenSubNodeIter.next();
                                        return true;
                                } else {
                                        this.next = null;
                                        this.doNext = ProcessStages.ProcessChildCurNode;
                                        return hasNext();
                                }
                        }

                        return false;
                }

                @Override
                public TreeNode<T> next() {
                        return this.next;
                }

                @Override
                public void remove() {
                        throw new UnsupportedOperationException();
                }

        }
}

